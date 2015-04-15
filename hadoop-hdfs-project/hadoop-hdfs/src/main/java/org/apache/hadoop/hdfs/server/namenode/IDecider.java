package org.apache.hadoop.hdfs.server.namenode;

import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_PERMISSIONS_SUPERUSERGROUP_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_PERMISSIONS_SUPERUSERGROUP_KEY;

import java.io.IOException;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CacheFlag;
import org.apache.hadoop.fs.UnresolvedLinkException;
import org.apache.hadoop.hdfs.protocol.CachePoolEntry;
import org.apache.hadoop.hdfs.protocol.CachePoolInfo;
import org.apache.hadoop.hdfs.server.namenode.FSNamesystem;
import org.apache.hadoop.fs.BatchedRemoteIterator.BatchedListEntries;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.hdfs.protocol.CacheDirectiveInfo;
import org.apache.hadoop.fs.Path;

public class IDecider {
	
	public static final Log LOG = LogFactory.getLog(IDecider.class);
	private static final String STAGEFILEPATTERN = "\\b.staging\\b";
	private static final int CACHETHRESHOLD = 3;
	private static final String POOL_NAME = "default";
	private static boolean isDefaultCachePresent = false;
	private final FsPermission MODE = new FsPermission((short)0755);
	
	private int cachedFlag = 0;
	private long accessCount = 0L;
	private boolean isCached = false;
	private boolean isStageFile = false;
	
	FSDirectory dir;
	Configuration conf;
	
	/*
	 * Default Constructor
	 */
	public IDecider(FSDirectory dir, Configuration conf) {
		this.dir = dir;
		this.conf = conf;
	}
	
	/*
	 *  Check whether file is eligible for caching. It compares the access count
	 *  of file with CACHETHRESHOLD.
	 */
	public final boolean checkFileEligiblityForCache(String src) throws UnresolvedLinkException, 
	IOException {
		accessCount = 0L;
		INode inode = dir.getINode(src);
		INodeFile inodeFile = INodeFile.valueOf(inode, src);
		accessCount = inodeFile.getAccessCount(null);
		LOG.info("The file " + src + "is accessed " + accessCount + "times");
		return (accessCount > (CACHETHRESHOLD - 1));
	}
	
	/*
	 *  Increment the access count of file on every read operation.
	 */
	public final void incrementAccessCount(String src) throws UnresolvedLinkException, 
	IOException {
		accessCount = 0L;
		INode inode = dir.getINode(src);
		INodeFile inodeFile = INodeFile.valueOf(inode, src);
		accessCount = inodeFile.getAccessCount(null);
		LOG.info("The file " + src + "is accessed " + accessCount + " times before increment");
		inodeFile.setAccessCount(accessCount + 1);
		LOG.info("The file " + src + "is accessed " + inodeFile.getAccessCount(null) 
				+ " times after increment");
	}
	
	/*
	 * Check whether file is under '.staging' dir. If yes return true else false.
	 */
	public final boolean isStageFile (String src) {
		if (src == null || src.isEmpty())
			return isStageFile;
		try { 
			Pattern p=Pattern.compile(STAGEFILEPATTERN);
	        Matcher m=p.matcher(src);
	        isStageFile = m.find();
		} catch (PatternSyntaxException pse) {
			LOG.warn("Regular expression '" + STAGEFILEPATTERN + "' for property '" +
		               src + "' not valid.", pse);
		}
		return isStageFile;
	}
	
	/*
	 *  Set the isCached flag based on the whether file is eligible for caching.
	 */
	public final void setIsCached(String src,boolean isCached) throws UnresolvedLinkException, 
	IOException {
		cachedFlag = 0;
		INode inode = dir.getINode(src);
		INodeFile inodeFile = INodeFile.valueOf(inode, src);
		LOG.info("The file before caching in setIsCached " + src + " is cached " + inodeFile.getIsCached(null));
		cachedFlag = (isCached) ? 1 : 0;
		inodeFile.setIsCached(cachedFlag);
		LOG.info("The file after caching in setIsCached " + src + " is cached " + inodeFile.getIsCached(null));
		this.isCached = isCached;
	}
	
	/*
	 *  Get the isCached flag.
	 */
	public final boolean isCached(String src) throws UnresolvedLinkException, 
	IOException {
		cachedFlag = 0;
		INode inode = dir.getINode(src);
		INodeFile inodeFile = INodeFile.valueOf(inode, src);
		cachedFlag = inodeFile.getIsCached(null);
		LOG.info("The file before caching in isCached " + src + " is cached " + cachedFlag);
		this.isCached = (cachedFlag == 1) ? true : false;
		LOG.info("The file before caching in isCached " + src + " is cached " + this.isCached);
		return this.isCached;
	}
	
	/* 
	 *  Cache the file with path specified in src parameter.
	 */
	public final long cacheFile(String src,FSNamesystem fsnamesystemObj) 
	throws IOException {
		
		String pool_name = getCachePoolForFile(fsnamesystemObj);
		CacheDirectiveInfo directiveInfo = new CacheDirectiveInfo.Builder()
										   .setPath(new Path(src))
										   .setReplication((short)1)
										   .setPool(pool_name).build();
		return fsnamesystemObj.addCacheDirective(directiveInfo, EnumSet.noneOf(CacheFlag.class));
	}
	
	/*
	 *  Get the Pool if exist. If not create a default pool.
	 */
	public final String getCachePoolForFile(FSNamesystem fsnamesystemObj)
	throws IOException {
		
		BatchedListEntries<CachePoolEntry> cachepool;
		LOG.info("Value of isDefaultCachePresent " + isDefaultCachePresent);
		if (!isDefaultCachePresent) {
			cachepool = fsnamesystemObj.listCachePools("");
			if(cachepool != null && cachepool.size() > 0) {
				LOG.info("Other Cache pool present ");
				if (checkDefaultPoolExist(cachepool))
					isDefaultCachePresent = true;
				else
					isDefaultCachePresent = false;
			} else {
				isDefaultCachePresent = false;
			}
			if (!isDefaultCachePresent) {
				LOG.info("Creating default cache pool ");
				createPool(fsnamesystemObj);
				isDefaultCachePresent = true;
			}
			LOG.info("Default Cache pool present");
		}
		return POOL_NAME;
	}
	
	/*
	 *  Check if default pool exist in already cache pool.
	 */
	public final boolean checkDefaultPoolExist(BatchedListEntries<CachePoolEntry> cachepool) {
		for (int i = 0; i < cachepool.size(); i++) {
			if (cachepool.get(i).getInfo().getPoolName().equalsIgnoreCase(POOL_NAME))
				return true;
	    }
		return false;
	}
	
	/*
	 *  Create the default cache pool if not already created.
	 */
	public final void createPool(FSNamesystem fsnamesystemObj)
	throws IOException {
		String fsOwner = UserGroupInformation.getCurrentUser().getShortUserName();
		String groupName = conf.get(DFS_PERMISSIONS_SUPERUSERGROUP_KEY,
									DFS_PERMISSIONS_SUPERUSERGROUP_DEFAULT);
		fsnamesystemObj.addCachePool(new CachePoolInfo(POOL_NAME).setOwnerName(fsOwner)
						 				 .setGroupName(groupName).setMode(MODE));
	}
	
	/*
	 *  Set the Access count and Is Cached flag to CACHETHRESHOLD/True respectively as 
	 *  directive is added through cacheadmin addDirective command api.
	 */
	public final void setAccessCountAndIsCachedFlag(String src)
	throws IOException {
		LOG.info(" Inside setAccessCountAndIsCachedFlag() ");
		long accesscount = 0L;
		int isCached = 0;
		INode inode = dir.getINode(src);
		INodeFile inodeFile = INodeFile.valueOf(inode, src);
		accesscount = inodeFile.getAccessCount(null);
		LOG.info("Value of accesscount in setAccessCountAndIsCachedFlag: " + accesscount);
		if (accesscount < CACHETHRESHOLD)
			inodeFile.setAccessCount(CACHETHRESHOLD);
		isCached = inodeFile.getIsCached(null);
		LOG.info("Value of isCached in setAccessCountAndIsCachedFlag: " + isCached);
		if (isCached == 0)
			inodeFile.setIsCached(1);
	}
}
