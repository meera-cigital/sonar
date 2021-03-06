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
package org.sonar.core.source;

class OpeningHtmlTag {

  private final int startOffset;
  private final String cssClass;

  OpeningHtmlTag(int startOffset, String cssClass) {
    this.startOffset = startOffset;
    this.cssClass = cssClass;
  }

  int getStartOffset() {
    return startOffset;
  }

  String getCssClass() {
    return cssClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpeningHtmlTag openingHtmlTag = (OpeningHtmlTag) o;
    if (startOffset != openingHtmlTag.startOffset) {
      return false;
    }
    if (cssClass != null ? !cssClass.equals(openingHtmlTag.cssClass) : openingHtmlTag.cssClass != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = startOffset;
    result = 31 * result + (cssClass != null ? cssClass.hashCode() : 0);
    return result;
  }
}
