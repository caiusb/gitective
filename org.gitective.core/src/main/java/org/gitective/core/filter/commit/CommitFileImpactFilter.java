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
package org.gitective.core.filter.commit;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.gitective.core.filter.commit.CommitImpact.DescendingImpactComparator;

/**
 * Filter that tracks the impact of commits measure in terms of files changed
 */
public class CommitFileImpactFilter extends CommitDiffFilter implements
		Iterable<CommitImpact> {

	private final int limit;

	private final SortedSet<CommitImpact> commits = new TreeSet<CommitImpact>(
			new DescendingImpactComparator());

	/**
	 * Create an impact filter that retains the given number of most impacting
	 * commits
	 *
	 * @param detectRenames
	 * @param limit
	 */
	public CommitFileImpactFilter(final boolean detectRenames, final int limit) {
		super(detectRenames);
		this.limit = limit;
	}

	/**
	 * Create an impact filter that tracks the 10 most impacting commits
	 *
	 * @param detectRenames
	 */
	public CommitFileImpactFilter(final boolean detectRenames) {
		this(detectRenames, 10);
	}

	/**
	 * Create an impact filter that tracks the 10 most impacting commits
	 */
	public CommitFileImpactFilter() {
		this(false);
	}

	/**
	 * Create an impact filter that retains the given number of most impacting
	 * commits
	 *
	 * @param limit
	 */
	public CommitFileImpactFilter(final int limit) {
		this(false, limit);
	}

	/**
	 * @return limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @return commits
	 */
	public SortedSet<CommitImpact> getCommits() {
		return commits;
	}

	@Override
	public boolean include(final RevCommit commit,
			final Collection<DiffEntry> diffs) {
		int add = 0;
		int edit = 0;
		int delete = 0;
		for (DiffEntry diff : diffs)
			switch (diff.getChangeType()) {
			case ADD:
				add++;
				break;
			case MODIFY:
				edit++;
				break;
			case DELETE:
				delete++;
				break;
			}
		final CommitImpact impact = new CommitImpact(commit, add, edit, delete);
		commits.add(impact);
		if (commits.size() > limit)
			commits.remove(commits.last());
		return true;
	}

	@Override
	public RevFilter clone() {
		return new CommitFileImpactFilter(detectRenames, limit);
	}

	@Override
	public CommitFilter reset() {
		commits.clear();
		return super.reset();
	}

	public Iterator<CommitImpact> iterator() {
		return commits.iterator();
	}
}
