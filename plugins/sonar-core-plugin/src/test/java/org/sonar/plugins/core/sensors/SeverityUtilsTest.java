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
package org.sonar.plugins.core.sensors;

import org.junit.Test;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rules.RulePriority;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SeverityUtilsTest {
  @Test
  public void shouldConvertToViolationMetric() {
    assertThat(SeverityUtils.severityToViolationMetric(RulePriority.INFO), is(CoreMetrics.INFO_VIOLATIONS));
    assertThat(SeverityUtils.severityToViolationMetric(RulePriority.BLOCKER), is(CoreMetrics.BLOCKER_VIOLATIONS));
  }
}
