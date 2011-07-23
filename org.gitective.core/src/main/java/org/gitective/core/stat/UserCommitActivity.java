/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package org.gitective.core.stat;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Commit user activity class
 */
public class UserCommitActivity implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 3567351283712945310L;

	/**
	 * Default size of expected commits
	 */
	public static final int SIZE = 16;

	/**
	 * Default growth percentage of expected commits
	 */
	public static final int GROWTH = 10;

	private final String name;
	private final String email;
	private int index;
	private byte[][] commits;
	private long[] times;
	private long first = Long.MAX_VALUE;
	private long last = Long.MIN_VALUE;

	/**
	 * Create user activity for given name and email
	 * 
	 * @param name
	 * @param email
	 */
	public UserCommitActivity(final String name, final String email) {
		this.name = name;
		this.email = email;
		commits = new byte[SIZE][];
		times = new long[SIZE];
	}

	/**
	 * Get name of user
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get email address of user
	 * 
	 * @return email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Include given commit in activity
	 * 
	 * @param commit
	 *            must be non-null
	 * @param user
	 *            must be non-null
	 * @return this activity
	 */
	public UserCommitActivity include(final RevCommit commit,
			final PersonIdent user) {
		final long when = user.getWhen().getTime();
		if (index == commits.length) {
			int newSize = commits.length;
			// Grow arrays by either GROWTH percentage or SIZE value, whichever
			// is higher
			newSize += Math.max(SIZE, (int) (newSize / GROWTH));
			commits = Arrays.copyOf(commits, newSize);
			times = Arrays.copyOf(times, newSize);
		}
		final byte[] id = new byte[Constants.OBJECT_ID_LENGTH];
		commit.copyRawTo(id, 0);
		commits[index] = id;
		times[index] = when;
		index++;

		if (when < first)
			first = when;
		if (when > last)
			last = when;
		return this;
	}

	/**
	 * Get commit times
	 * 
	 * @return non-null but possibly empty array
	 */
	public long[] getTimes() {
		return Arrays.copyOf(times, index);
	}

	/**
	 * Get raw commits as array of byte arrays
	 * 
	 * @return non-null but possibly empty array
	 */
	public byte[][] getRawIds() {
		return Arrays.copyOf(commits, index);
	}

	/**
	 * Get commits as array of object ids
	 * 
	 * @return non-null but possibly empty array
	 */
	public ObjectId[] getIds() {
		final ObjectId[] ids = new ObjectId[index];
		for (int i = 0; i < index; i++)
			ids[i] = ObjectId.fromRaw(commits[i]);
		return ids;
	}

	/**
	 * Get number of commits
	 * 
	 * @return commit count
	 */
	public int getCount() {
		return index;
	}

	/**
	 * Get id of first commit
	 * 
	 * @return commit id or null if no commits
	 */
	public ObjectId getFirst() {
		return index > 0 ? ObjectId.fromRaw(commits[index - 1]) : null;
	}

	/**
	 * Get id of latest commit
	 * 
	 * @return commit id or null if no commits
	 */
	public ObjectId getLast() {
		return index > 0 ? ObjectId.fromRaw(commits[0]) : null;
	}

	/**
	 * Get time of earliest commit
	 * 
	 * @return time in milliseconds or 0 if no commits
	 */
	public long getEarliest() {
		return index > 0 ? first : 0;
	}

	/**
	 * Get time of latest commit
	 * 
	 * @return time in milliseconds or 0 if no commits
	 */
	public long getLatest() {
		return index > 0 ? last : 0;
	}
}
