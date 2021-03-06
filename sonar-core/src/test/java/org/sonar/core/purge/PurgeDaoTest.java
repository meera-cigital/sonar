/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.core.purge;

import org.apache.ibatis.session.SqlSession;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.Scopes;
import org.sonar.core.persistence.AbstractDaoTestCase;
import org.sonar.core.persistence.MyBatis;
import org.sonar.core.resource.ResourceDao;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

public class PurgeDaoTest extends AbstractDaoTestCase {

  private PurgeDao dao;

  @Before
  public void createDao() {
    dao = new PurgeDao(getMyBatis(), new ResourceDao(getMyBatis()), new PurgeProfiler());
  }

  @Test
  public void shouldDeleteAbortedBuilds() {
    setupData("shouldDeleteAbortedBuilds");
    dao.purge(1L, new String[0]);
    checkTables("shouldDeleteAbortedBuilds", "snapshots");
  }

  @Test
  public void shouldCloseReviewWhenDisablingResource() {
    setupData("shouldCloseReviewWhenDisablingResource");

    SqlSession session = getMyBatis().openSession();
    try {
      dao.disableResource(1L, session.getMapper(PurgeMapper.class));

      // the above method does not commit and close the session
      session.commit();

    } finally {
      MyBatis.closeQuietly(session);
    }
    checkTables("shouldCloseReviewWhenDisablingResource", /* excluded column */new String[] {"updated_at"}, "reviews");
  }

  @Test
  public void shouldPurgeProject() {
    setupData("shouldPurgeProject");
    dao.purge(1, new String[0]);
    checkTables("shouldPurgeProject", "projects", "snapshots");
  }

  @Test
  public void shouldDeleteHistoricalDataOfDirectoriesAndFiles() {
    setupData("shouldDeleteHistoricalDataOfDirectoriesAndFiles");
    dao.purge(1, new String[] {Scopes.DIRECTORY, Scopes.FILE});
    checkTables("shouldDeleteHistoricalDataOfDirectoriesAndFiles", "projects", "snapshots");
  }

  @Test
  public void shouldDisableResourcesWithoutLastSnapshot() {
    setupData("shouldDisableResourcesWithoutLastSnapshot");
    dao.purge(1, new String[0]);
    checkTables("shouldDisableResourcesWithoutLastSnapshot", "projects", "snapshots");
  }

  @Test
  public void shouldDeleteSnapshots() {
    setupData("shouldDeleteSnapshots");
    dao.deleteSnapshots(PurgeSnapshotQuery.create().setIslast(false).setResourceId(1L));
    checkTables("shouldDeleteSnapshots", "snapshots");
  }

  @Test
  public void shouldSelectPurgeableSnapshots() {
    setupData("shouldSelectPurgeableSnapshots");
    List<PurgeableSnapshotDto> snapshots = dao.selectPurgeableSnapshots(1L);

    assertThat(snapshots, hasItem(new SnapshotMatcher(1L, true, false)));
    assertThat(snapshots, hasItem(new SnapshotMatcher(4L, false, false)));
    assertThat(snapshots, hasItem(new SnapshotMatcher(5L, false, true)));
    assertThat(snapshots.size(), is(3));
  }

  @Test
  public void shouldDeleteProject() {
    setupData("shouldDeleteProject");
    dao.deleteResourceTree(1L);
    assertEmptyTables("projects", "snapshots", "action_plans", "action_plans_reviews", "reviews", "review_comments");
  }

  static final class SnapshotMatcher extends BaseMatcher<PurgeableSnapshotDto> {
    long snapshotId;
    boolean isLast;
    boolean hasEvents;

    SnapshotMatcher(long snapshotId, boolean last, boolean hasEvents) {
      this.snapshotId = snapshotId;
      this.isLast = last;
      this.hasEvents = hasEvents;
    }

    public boolean matches(Object o) {
      PurgeableSnapshotDto obj = (PurgeableSnapshotDto) o;
      return obj.getSnapshotId() == snapshotId && obj.isLast() == isLast && obj.hasEvents() == hasEvents;
    }

    public void describeTo(Description description) {
      description
          .appendText("snapshotId").appendValue(snapshotId)
          .appendText("isLast").appendValue(isLast)
          .appendText("hasEvents").appendValue(hasEvents);
    }
  }
}
