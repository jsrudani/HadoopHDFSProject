package org.apache.hadoop.hdfs.server.namenode;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.UnresolvedLinkException;
import org.apache.hadoop.hdfs.server.namenode.FSNamesystem;

public class IDecider {
	
	public static final Log LOG = LogFactory.getLog(IDecider.class);
	private static final String STAGEFILEPATTERN = "\\b.staging\\b";
	private boolean isStageFile = false;
	private static final int CACHETHRESHOLD = 3;
	private long accessCount = 0L;
	private int cachedFlag = 0;
	private boolean isCached = false;
	FSDirectory dir;
	
	/*
	 * Default Constructor
	 */
	public IDecider(FSDirectory dir) {
		this.dir = dir;
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
		LOG.info("The file " + src + "is accessed " + accessCount + "times before increment");
		inodeFile.setAccessCount(accessCount + 1);
		LOG.info("The file " + src + "is accessed " + inodeFile.getAccessCount(null) 
				+ "times after increment");
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
}
