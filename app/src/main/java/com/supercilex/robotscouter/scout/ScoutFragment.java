package com.supercilex.robotscouter.scout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.supercilex.robotscouter.Constants;
import com.supercilex.robotscouter.FirebaseRecyclerAdapter;
import com.supercilex.robotscouter.R;
import com.supercilex.robotscouter.Utils;
import com.supercilex.robotscouter.model.scout.metrics.ScoutCheckbox;
import com.supercilex.robotscouter.model.scout.metrics.ScoutCounter;
import com.supercilex.robotscouter.model.scout.metrics.ScoutEditText;
import com.supercilex.robotscouter.model.scout.metrics.ScoutMetric;
import com.supercilex.robotscouter.model.scout.metrics.ScoutSpinner;
import com.supercilex.robotscouter.model.scout.viewholders.CheckboxViewHolder;
import com.supercilex.robotscouter.model.scout.viewholders.CounterViewHolder;
import com.supercilex.robotscouter.model.scout.viewholders.EditTextViewHolder;
import com.supercilex.robotscouter.model.scout.viewholders.ScoutViewHolder;
import com.supercilex.robotscouter.model.scout.viewholders.SpinnerViewHolder;

public class ScoutFragment extends Fragment {
    private static final String ARG_SCOUT_KEY = "scout_key";

    private FirebaseRecyclerAdapter mAdapter;

    public static ScoutFragment newInstance(String key) {

        ScoutFragment fragment = new ScoutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SCOUT_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.current_scout_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.current_scout_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        mAdapter = new FirebaseRecyclerAdapter<ScoutMetric, ScoutViewHolder>(
                ScoutMetric.class,
                R.layout.activity_main_row_layout,
                ScoutViewHolder.class,
                Utils.getDatabase()
                        .getReference()
                        .child(Constants.FIREBASE_SCOUTS)
                        .child(getArguments().getString(ARG_SCOUT_KEY))
                        .child(Constants.FIREBASE_VIEWS)) {
            @Override
            public void populateViewHolder(ScoutViewHolder viewHolder,
                                           ScoutMetric view,
                                           int position) {
                viewHolder.initialize(view, getRef(position));
            }

            @Override
            protected void onCancelled(DatabaseError databaseError) {
                FirebaseCrash.report(databaseError.toException());
            }

            @Override
            protected ScoutMetric parseSnapshot(DataSnapshot snapshot) {
                int viewType = snapshot.child(Constants.FIREBASE_TYPE).getValue(Integer.class);

                if (viewType == Constants.CHECKBOX) {
                    return snapshot.getValue(ScoutCheckbox.class);
                } else if (viewType == Constants.COUNTER) {
                    return snapshot.getValue(ScoutCounter.class);
                } else if (viewType == Constants.SPINNER) {
                    return snapshot.getValue(ScoutSpinner.class);
                } else if (viewType == Constants.EDIT_TEXT) {
                    return snapshot.getValue(ScoutEditText.class);
                }

                throw new IllegalArgumentException("Scout class not found at parseSnapshot");
            }

            @Override
            public ScoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case Constants.CHECKBOX:
                        return new CheckboxViewHolder(LayoutInflater.from(parent.getContext())
                                                              .inflate(R.layout.checkbox,
                                                                       parent,
                                                                       false));
                    case Constants.COUNTER:
                        return new CounterViewHolder(LayoutInflater.from(parent.getContext())
                                                             .inflate(R.layout.counter,
                                                                      parent,
                                                                      false));
                    case Constants.SPINNER:
                        return new SpinnerViewHolder(LayoutInflater.from(parent.getContext())
                                                             .inflate(R.layout.spinner,
                                                                      parent,
                                                                      false));
                    case Constants.EDIT_TEXT:
                        return new EditTextViewHolder(LayoutInflater.from(parent.getContext())
                                                              .inflate(R.layout.edittext,
                                                                       parent,
                                                                       false));
                    default:
                        FirebaseCrash.report(new IllegalStateException(
                                "Incomplete case statement: onCreateViewHolder"));
                        return null;
                }
            }

            @Override
            public int getItemViewType(int position) {
                return getItem(position).getType();
            }
        };

        recyclerView.setAdapter(mAdapter);
        return rootView;
    }
}