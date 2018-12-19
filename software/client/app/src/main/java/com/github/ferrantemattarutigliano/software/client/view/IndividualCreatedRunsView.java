package com.github.ferrantemattarutigliano.software.client.view;

import com.github.ferrantemattarutigliano.software.client.model.RunDTO;

import java.util.Collection;

public interface IndividualCreatedRunsView {
    void onRunFetch(Collection<RunDTO> output);
    void noCreatedRuns();
    void onDeleteRun(String message);
}
