package com.eactive.resourcehub.project.controller;

import com.eactive.resourcehub.project.entity.ProjectAssignment;
import com.eactive.resourcehub.user.entity.User;

record PersonnelStatusItem(User user, ProjectAssignment activeAssignment) {}
