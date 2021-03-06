// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.change;

import com.google.common.base.Strings;
import com.google.gerrit.common.data.Capable;
import com.google.gerrit.extensions.api.changes.CherryPickInput;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.BadRequestException;
import com.google.gerrit.extensions.restapi.ResourceConflictException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.RestModifyView;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.reviewdb.client.RefNames;
import com.google.gerrit.server.git.IntegrationException;
import com.google.gerrit.server.project.CommitResource;
import com.google.gerrit.server.project.InvalidChangeOperationException;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.RefControl;
import com.google.gerrit.server.update.UpdateException;
import com.google.gwtorm.server.OrmException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.jgit.revwalk.RevCommit;

@Singleton
public class CherryPickCommit implements RestModifyView<CommitResource, CherryPickInput> {

  private final CherryPickChange cherryPickChange;
  private final ChangeJson.Factory json;

  @Inject
  CherryPickCommit(CherryPickChange cherryPickChange, ChangeJson.Factory json) {
    this.cherryPickChange = cherryPickChange;
    this.json = json;
  }

  @Override
  public ChangeInfo apply(CommitResource rsrc, CherryPickInput input)
      throws OrmException, IOException, UpdateException, RestApiException {
    String message = Strings.nullToEmpty(input.message).trim();
    String destination = Strings.nullToEmpty(input.destination).trim();
    int parent = input.parent == null ? 1 : input.parent;

    if (destination.isEmpty()) {
      throw new BadRequestException("destination must be non-empty");
    }

    ProjectControl projectControl = rsrc.getProject();
    Capable capable = projectControl.canPushToAtLeastOneRef();
    if (capable != Capable.OK) {
      throw new AuthException(capable.getMessage());
    }

    RevCommit commit = rsrc.getCommit();
    String refName = RefNames.fullName(destination);
    RefControl refControl = projectControl.controlForRef(refName);
    if (!refControl.canUpload()) {
      throw new AuthException("Not allowed to cherry pick " + commit + " to " + destination);
    }

    Project.NameKey project = projectControl.getProject().getNameKey();
    try {
      Change.Id cherryPickedChangeId =
          cherryPickChange.cherryPick(
              null,
              null,
              null,
              null,
              project,
              commit,
              message.isEmpty() ? commit.getFullMessage() : message,
              refName,
              refControl,
              parent);
      return json.noOptions().format(project, cherryPickedChangeId);
    } catch (InvalidChangeOperationException e) {
      throw new BadRequestException(e.getMessage());
    } catch (IntegrationException e) {
      throw new ResourceConflictException(e.getMessage());
    }
  }
}
