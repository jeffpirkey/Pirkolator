package com.rws.pirkolator.core.transform;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.rws.pirkolator.model.filter.IFilter;
import com.rws.pirkolator.model.filter.NameFilter;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.pirkolator.view.model.FilterView;
import com.rws.utility.common.UUIDs;

public class FilterViewFromIFilterFunction extends AbstractTransformFunction<IFilter, FilterView> {

    protected FilterViewFromIFilterFunction () {

        super (IFilter.class, FilterView.class);
    }

    @Override
    public FilterView apply (@Nullable final IFilter input) {

        checkNotNull (input);

        String desc;
        String label;
        String type;
        final Map<String, String> map = Maps.newConcurrentMap ();
        if (input instanceof NameFilter) {

            desc = "Name Filter";
            label = "Name Filter";
            type = "Name Filter";

            int idx = 0;
            for (final String name : ((NameFilter) input).getNameSet ()) {
                map.put ("name-" + idx, name);
                idx++;
            }
        } else if (input instanceof TypeFilter) {

            desc = "Type Filter";
            label = "Type Filter";
            type = "Type Filter";

            int idx = 0;
            for (final String name : ((NameFilter) input).getNameSet ()) {
                map.put ("type-" + idx, name);
                idx++;
            }
        } else {
            desc = "No description";
            label = "Unknown filter";
            type = "Unknown";
        }

        final FilterView view = new FilterView (UUIDs.generateUUIDAsString (), label, type, desc, map);

        return view;
    }
}
