/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.server.namenode;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.fs.permission.PermissionStatus;
import org.apache.hadoop.hdfs.protocol.QuotaExceededException;
import org.apache.hadoop.hdfs.server.namenode.snapshot.Snapshot;
import org.apache.hadoop.util.LightWeightGSet.LinkedElement;

import com.google.common.base.Preconditions;

/**
 * {@link INode} with additional fields including id, name, permission,
 * access time and modification time.
 */
@InterfaceAudience.Private
public abstract class INodeWithAdditionalFields extends INode
    implements LinkedElement {
  static enum PermissionStatusFormat {
    MODE(0, 16),
    GROUP(MODE.OFFSET + MODE.LENGTH, 25),
    USER(GROUP.OFFSET + GROUP.LENGTH, 23);

    final int OFFSET;
    final int LENGTH; //bit length
    final long MASK;

    PermissionStatusFormat(int offset, int length) {
      OFFSET = offset;
      LENGTH = length;
      MASK = ((-1L) >>> (64 - LENGTH)) << OFFSET;
    }

    long retrieve(long record) {
      return (record & MASK) >>> OFFSET;
    }

    long combine(long bits, long record) {
      return (record & ~MASK) | (bits << OFFSET);
    }

    /** Encode the {@link PermissionStatus} to a long. */
    static long toLong(PermissionStatus ps) {
      long permission = 0L;
      final int user = SerialNumberManager.INSTANCE.getUserSerialNumber(
          ps.getUserName());
      permission = USER.combine(user, permission);
      final int group = SerialNumberManager.INSTANCE.getGroupSerialNumber(
          ps.getGroupName());
      permission = GROUP.combine(group, permission);
      final int mode = ps.getPermission().toShort();
      permission = MODE.combine(mode, permission);
      return permission;
    }
  }

  /** The inode id. */
  final private long id;
  /**
   *  The inode name is in java UTF8 encoding; 
   *  The name in HdfsFileStatus should keep the same encoding as this.
   *  if this encoding is changed, implicitly getFileInfo and listStatus in
   *  clientProtocol are changed; The decoding at the client
   *  side should change accordingly.
   */
  private byte[] name = null;
  /** 
   * Permission encoded using {@link PermissionStatusFormat}.
   * Codes other than {@link #clonePermissionStatus(INodeWithAdditionalFields)}
   * and {@link #updatePermissionStatus(PermissionStatusFormat, long)}
   * should not modify it.
   */
  private long permission = 0L;
  /** The last modification time*/
  private long modificationTime = 0L;
  /** The last access time*/
  private long accessTime = 0L;
  /** The access count IDecider - 03/23/2015 - 1:44 PM. */
  private long accessCount = 0L;
  /** The iscached indicates file is cached or not IDecider - 03/29/2015 - 2:15 AM. */
  private int isCached = 0;

  /** For implementing {@link LinkedElement}. */
  private LinkedElement next = null;

  private INodeWithAdditionalFields(INode parent, long id, byte[] name,
      long permission, long modificationTime, long accessTime, long accessCount, int isCached) {
    super(parent);
    this.id = id;
    this.name = name;
    this.permission = permission;
    this.modificationTime = modificationTime;
    this.accessTime = accessTime;
    this.accessCount = accessCount;
    this.isCached = isCached;
  }

  INodeWithAdditionalFields(long id, byte[] name, PermissionStatus permissions,
      long modificationTime, long accessTime) {
    this(null, id, name, PermissionStatusFormat.toLong(permissions),
        modificationTime, accessTime,0L,0);
  }
  
  // Overload the constructor with access count and isCached parameters IDecider - 03/29/2015 12:35PM
  INodeWithAdditionalFields(long id, byte[] name, PermissionStatus permissions,
	  long modificationTime, long accessTime, long accessCount, int isCached) {
	this(null, id, name, PermissionStatusFormat.toLong(permissions),
	    modificationTime, accessTime,accessCount,isCached);
  }
  
  /** @param other Other node to be copied */
  INodeWithAdditionalFields(INodeWithAdditionalFields other) {
    this(other.getParentReference() != null ? other.getParentReference()
        : other.getParent(), other.getId(), other.getLocalNameBytes(),
        other.permission, other.modificationTime, other.accessTime,
        other.accessCount, other.isCached);
  }

  @Override
  public void setNext(LinkedElement next) {
    this.next = next;
  }
  
  @Override
  public LinkedElement getNext() {
    return next;
  }

  /** Get inode id */
  @Override
  public final long getId() {
    return this.id;
  }

  @Override
  public final byte[] getLocalNameBytes() {
    return name;
  }
  
  @Override
  public final void setLocalName(byte[] name) {
    this.name = name;
  }

  /** Clone the {@link PermissionStatus}. */
  final void clonePermissionStatus(INodeWithAdditionalFields that) {
    this.permission = that.permission;
  }

  @Override
  final PermissionStatus getPermissionStatus(Snapshot snapshot) {
    return new PermissionStatus(getUserName(snapshot), getGroupName(snapshot),
        getFsPermission(snapshot));
  }

  private final void updatePermissionStatus(PermissionStatusFormat f, long n) {
    this.permission = f.combine(n, permission);
  }

  @Override
  final String getUserName(Snapshot snapshot) {
    if (snapshot != null) {
      return getSnapshotINode(snapshot).getUserName();
    }

    int n = (int)PermissionStatusFormat.USER.retrieve(permission);
    return SerialNumberManager.INSTANCE.getUser(n);
  }

  @Override
  final void setUser(String user) {
    int n = SerialNumberManager.INSTANCE.getUserSerialNumber(user);
    updatePermissionStatus(PermissionStatusFormat.USER, n);
  }

  @Override
  final String getGroupName(Snapshot snapshot) {
    if (snapshot != null) {
      return getSnapshotINode(snapshot).getGroupName();
    }

    int n = (int)PermissionStatusFormat.GROUP.retrieve(permission);
    return SerialNumberManager.INSTANCE.getGroup(n);
  }

  @Override
  final void setGroup(String group) {
    int n = SerialNumberManager.INSTANCE.getGroupSerialNumber(group);
    updatePermissionStatus(PermissionStatusFormat.GROUP, n);
  }

  @Override
  final FsPermission getFsPermission(Snapshot snapshot) {
    if (snapshot != null) {
      return getSnapshotINode(snapshot).getFsPermission();
    }

    return new FsPermission(getFsPermissionShort());
  }

  @Override
  public final short getFsPermissionShort() {
    return (short)PermissionStatusFormat.MODE.retrieve(permission);
  }
  @Override
  void setPermission(FsPermission permission) {
    final short mode = permission.toShort();
    updatePermissionStatus(PermissionStatusFormat.MODE, mode);
  }

  @Override
  public long getPermissionLong() {
    return permission;
  }

  @Override
  final long getModificationTime(Snapshot snapshot) {
    if (snapshot != null) {
      return getSnapshotINode(snapshot).getModificationTime();
    }

    return this.modificationTime;
  }


  /** Update modification time if it is larger than the current value. */
  @Override
  public final INode updateModificationTime(long mtime, Snapshot latest,
      final INodeMap inodeMap) throws QuotaExceededException {
    Preconditions.checkState(isDirectory());
    if (mtime <= modificationTime) {
      return this;
    }
    return setModificationTime(mtime, latest, inodeMap);
  }

  final void cloneModificationTime(INodeWithAdditionalFields that) {
    this.modificationTime = that.modificationTime;
  }

  @Override
  public final void setModificationTime(long modificationTime) {
    this.modificationTime = modificationTime;
  }

  @Override
  final long getAccessTime(Snapshot snapshot) {
    if (snapshot != null) {
      return getSnapshotINode(snapshot).getAccessTime();
    }

    return accessTime;
  }

  /**
   * Set last access time of inode.
   */
  @Override
  public final void setAccessTime(long accessTime) {
    this.accessTime = accessTime;
  }
  
  // Added Access Count variable Getter/Setter IDecider - 03/23/2015 - 4:25PM
  public final long getAccessCount(Snapshot snapshot) {
	  if (snapshot != null) {
	      return getSnapshotINode(snapshot).getAccessCount();
	  }
	  return accessCount;
  }  
  /** Set access count of inode. */
  public final void setAccessCount(long accessCount) {
	  this.accessCount = accessCount;
  }
  
  // Added Cached Flag Getter/Setter IDecider - 03/29/2015 - 2:15AM
  /** Set cached flag to indicate whether File is cached or not. 
   *  1 indicates File is cached
   *  0 indicates File is not cached */
  public final void setIsCached(int isCachedFlag) {
	  this.isCached = isCachedFlag;
  }  
  /** Get cached flag of inode. */
  public final int getIsCached(Snapshot snapshot) {
	  if (snapshot != null) {
	      return getSnapshotINode(snapshot).getIsCached();
	  }
	  return isCached;
  }
  
}
