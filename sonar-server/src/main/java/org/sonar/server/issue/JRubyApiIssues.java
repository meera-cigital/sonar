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
package org.sonar.server.issue;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.sonar.api.issue.IssueFinder;
import org.sonar.api.issue.IssueQuery;
import org.sonar.api.issue.JRubyIssues;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.DateUtils;
import org.sonar.api.web.UserRole;
import org.sonar.server.platform.UserSession;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Facade of issue components for JRuby on Rails webapp
 *
 * @since 3.6
 */
public class JRubyApiIssues implements JRubyIssues {

  private final IssueFinder finder;

  public JRubyApiIssues(IssueFinder f) {
    this.finder = f;
  }

  /**
   * Requires the role {@link org.sonar.api.web.UserRole#CODEVIEWER}
   */
  @Override
  public IssueFinder.Results find(Map<String, Object> params) {
    // TODO move the role to IssueFinder
    return finder.find(toQuery(params), UserSession.get().userId(), UserRole.CODEVIEWER);
  }

  IssueQuery toQuery(Map<String, Object> props) {
    IssueQuery.Builder builder = IssueQuery.builder();
    builder.keys(toStrings(props.get("keys")));
    builder.severities(toStrings(props.get("severities")));
    builder.statuses(toStrings(props.get("statuses")));
    builder.resolutions(toStrings(props.get("resolutions")));
    builder.components(toStrings(props.get("components")));
    builder.componentRoots(toStrings(props.get("componentRoots")));
    builder.rules(toRules(props.get("rules")));
    builder.userLogins(toStrings(props.get("userLogins")));
    builder.assignees(toStrings(props.get("assignees")));
    builder.createdAfter(toDate(props.get("createdAfter")));
    builder.createdBefore(toDate(props.get("createdBefore")));
    builder.pageSize(toInteger(props.get("pageSize")));
    builder.pageIndex(toInteger(props.get("pageIndex")));
    return builder.build();
  }

  @SuppressWarnings("unchecked")
  static Collection<RuleKey> toRules(Object o) {
    Collection<RuleKey> result = null;
    if (o != null) {
      if (o instanceof List) {
        // assume that it contains only strings
        result = stringsToRules((List<String>) o);
      } else if (o instanceof String) {
        result = stringsToRules(Lists.newArrayList(Splitter.on(',').omitEmptyStrings().split((String) o)));
      }
    }
    return result;
  }

  private static Collection<RuleKey> stringsToRules(Collection<String> o) {
    return Collections2.transform(o, new Function<String, RuleKey>() {
      @Override
      public RuleKey apply(@Nullable String s) {
        return s != null ? RuleKey.parse(s) : null;
      }
    });
  }

  static List<String> toStrings(Object o) {
    List<String> result = null;
    if (o != null) {
      if (o instanceof List) {
        // assume that it contains only strings
        result = (List) o;
      } else if (o instanceof CharSequence) {
        result = Lists.newArrayList(Splitter.on(',').omitEmptyStrings().split((CharSequence) o));
      }
    }
    return result;
  }

  Integer toInteger(Object o) {
    if (o instanceof Integer) {
      return (Integer) o;
    }
    if (o instanceof Long) {
      return Ints.checkedCast((Long) o);
    }

    if (o instanceof String) {
      return Integer.parseInt((String) o);
    }
    return null;
  }

  Double toDouble(Object o) {
    if (o instanceof Double) {
      return (Double) o;
    }
    if (o instanceof String) {
      return Double.parseDouble((String) o);
    }
    return null;
  }


  Date toDate(Object o) {
    if (o instanceof Date) {
      return (Date) o;
    }
    if (o instanceof String) {
      return DateUtils.parseDateTime((String) o);
    }
    return null;
  }

  public void start() {
    // used to force pico to instantiate the singleton at startup
  }
}
