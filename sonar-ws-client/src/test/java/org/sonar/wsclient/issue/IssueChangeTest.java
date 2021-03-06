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
package org.sonar.wsclient.issue;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

public class IssueChangeTest {
  @Test
  public void should_create_empty_change() {
    IssueChange change = IssueChange.create();
    assertThat(change.urlParams()).isEmpty();
  }

  @Test
  public void should_create_change() {
    IssueChange change = IssueChange.create()
      .comment("this is a comment")
      .assignee("lancelot")
      .attribute("JIRA", "FOO-1234")
      .attribute("LINK", null)
      .cost(4.2)
      .transition("resolve")
      .severity("BLOCKER");
    assertThat(change.urlParams()).hasSize(7).includes(
      entry("comment", "this is a comment"),
      entry("newAssignee", "lancelot"),
      entry("newAttr[JIRA]", "FOO-1234"),
      entry("newAttr[LINK]", ""),
      entry("newCost", 4.2),
      entry("transition", "resolve"),
      entry("newSeverity", "BLOCKER")
    );
  }
}
