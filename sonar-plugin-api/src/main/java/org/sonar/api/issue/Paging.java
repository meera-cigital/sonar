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

package org.sonar.api.issue;

/**
 * TODO move outside this package
 * @since 3.6
 */
public class Paging {

  private final int pageSize;
  private final int pageIndex;
  private final int total;

  public Paging(int pageSize, int pageIndex, int total) {
    this.pageSize = pageSize;
    this.pageIndex = pageIndex;
    this.total = total;
  }

  public int pageIndex() {
    return pageIndex;
  }

  public int pageSize() {
    return pageSize;
  }

  public int total() {
    return total;
  }

  public int offset(){
    return (pageIndex - 1) * pageSize;
  }

  public int pages() {
    int p = (total / pageSize);
    if ((total % pageSize) > 0) {
      p++;
    }
    return p;
  }
}
