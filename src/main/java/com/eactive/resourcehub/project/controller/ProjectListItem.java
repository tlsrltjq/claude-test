package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.project.entity.Project;
import com.eactive.resourcehub.user.entity.User;

record ProjectListItem(Project project, User lead, int totalCount) {}
