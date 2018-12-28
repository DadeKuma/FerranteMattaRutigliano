package com.github.ferrantemattarutigliano.software.client.view.individual;

import com.github.ferrantemattarutigliano.software.client.model.IndividualDTO;

public interface IndividualHomeView {
    void notifyUser();
    void onProfileFetch(IndividualDTO individualDTO);
}