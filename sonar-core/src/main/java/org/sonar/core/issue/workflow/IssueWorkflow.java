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
package org.sonar.core.issue.workflow;

import org.picocontainer.Startable;
import org.sonar.api.BatchComponent;
import org.sonar.api.ServerComponent;
import org.sonar.api.issue.DefaultTransitions;
import org.sonar.api.issue.Issue;
import org.sonar.core.issue.DefaultIssue;

import java.util.List;

public class IssueWorkflow implements BatchComponent, ServerComponent, Startable {

  private StateMachine machine;

  @Override
  public void start() {
    machine = StateMachine.builder()
      .states(Issue.STATUS_OPEN, Issue.STATUS_REOPENED, Issue.STATUS_RESOLVED, Issue.STATUS_CLOSED)
      .transition(Transition.builder(DefaultTransitions.CLOSE)
        .from(Issue.STATUS_OPEN).to(Issue.STATUS_CLOSED)
        .functions(new SetResolution(Issue.RESOLUTION_FIXED), SetClosedAt.CLOSED_AT)
        .build())
      .transition(Transition.builder(DefaultTransitions.CLOSE)
        .from(Issue.STATUS_RESOLVED).to(Issue.STATUS_CLOSED)
        .functions(SetClosedAt.CLOSED_AT)
        .build())
      .transition(Transition.builder(DefaultTransitions.CLOSE)
        .from(Issue.STATUS_REOPENED).to(Issue.STATUS_CLOSED)
        .functions(SetClosedAt.CLOSED_AT)
        .functions(new SetResolution(Issue.RESOLUTION_FIXED))
        .build())
      .transition(Transition.builder(DefaultTransitions.RESOLVE)
        .from(Issue.STATUS_OPEN).to(Issue.STATUS_RESOLVED)
        .functions(new SetResolution(Issue.RESOLUTION_FIXED))
        .build())
      .transition(Transition.builder(DefaultTransitions.RESOLVE)
        .from(Issue.STATUS_REOPENED).to(Issue.STATUS_RESOLVED)
        .functions(new SetResolution(Issue.RESOLUTION_FIXED))
        .build())
      .transition(Transition.builder(DefaultTransitions.REOPEN)
        .from(Issue.STATUS_RESOLVED).to(Issue.STATUS_REOPENED)
        .functions(new SetResolution(Issue.RESOLUTION_OPEN))
        .build())
      .transition(Transition.builder(DefaultTransitions.REOPEN)
        .from(Issue.STATUS_CLOSED).to(Issue.STATUS_REOPENED)
        .functions(new SetResolution(Issue.RESOLUTION_OPEN), new UnsetClosedAt())
        .build())
      .transition(Transition.builder(DefaultTransitions.FALSE_POSITIVE)
        .from(Issue.STATUS_OPEN).to(Issue.STATUS_RESOLVED)
        .conditions(new IsManual(false))
        .functions(new SetResolution(Issue.RESOLUTION_FALSE_POSITIVE))
        .build())
      .transition(Transition.builder(DefaultTransitions.FALSE_POSITIVE)
        .from(Issue.STATUS_REOPENED).to(Issue.STATUS_RESOLVED)
        .conditions(new IsManual(false))
        .functions(new SetResolution(Issue.RESOLUTION_FALSE_POSITIVE))
        .build())

        // automatic transitions

        // Close the issues that do not exist anymore. Note that isAlive() is true on manual issues
      .transition(Transition.builder("automaticclose")
        .from(Issue.STATUS_OPEN).to(Issue.STATUS_CLOSED)
        .conditions(new IsAlive(false), new IsManual(false))
        .functions(new SetResolution(Issue.RESOLUTION_FIXED), SetClosedAt.CLOSED_AT)
        .automatic()
        .build())
      .transition(Transition.builder("automaticclose")
        .from(Issue.STATUS_REOPENED).to(Issue.STATUS_CLOSED)
        .conditions(new IsAlive(false))
        .functions(new SetResolution(Issue.RESOLUTION_FIXED), SetClosedAt.CLOSED_AT)
        .automatic()
        .build())
        // Close the issues marked as resolved and that do not exist anymore.
        // Note that false-positives are kept resolved and are not closed.
      .transition(Transition.builder("automaticclose")
        .from(Issue.STATUS_RESOLVED).to(Issue.STATUS_CLOSED)
        .conditions(new IsAlive(false))
        .functions(SetClosedAt.CLOSED_AT)
        .automatic()
        .build())
      .transition(Transition.builder("automaticreopen")
        .from(Issue.STATUS_RESOLVED).to(Issue.STATUS_REOPENED)
        .conditions(new IsAlive(true), new HasResolution(Issue.RESOLUTION_FIXED))
        .functions(new SetResolution(Issue.RESOLUTION_OPEN))
        .automatic()
        .build())
      .build();
  }

  @Override
  public void stop() {
  }

  public List<Transition> outManualTransitions(Issue issue) {
    return machine.state(issue.status()).outManualTransitions(issue);
  }

  public void doAutomaticTransition(DefaultIssue issue) {
    Transition transition = machine.state(issue.status()).outAutomaticTransition(issue);
    if (transition != null) {
      transition.execute(issue);
    }
  }


  StateMachine machine() {
    return machine;
  }
}
