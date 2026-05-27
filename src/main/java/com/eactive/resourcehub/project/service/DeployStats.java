package com.eactive.resourcehub.project.service;

public record DeployStats(
    long startingThisMonth,
    long endingThisMonth,
    long currentlyDeployed,
    long notDeployed
) {}
