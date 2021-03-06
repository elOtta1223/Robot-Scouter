package com.supercilex.robotscouter.ui.teamlist;

import com.supercilex.robotscouter.data.model.Team;

public interface TeamSelectionListener {
    void onTeamSelected(Team team, boolean addScout);

    void saveSelection(Team team);
}
