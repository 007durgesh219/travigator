package com.frodo.travigator.adapter;

/**
 * Created by Kapil on 9/6/2015.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.frodo.travigator.Favorite;
import com.frodo.travigator.activities.HomeActivity;

/**
 * Created by Kapil on 9/6/2015.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter{



    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
            case 0:
                // Top Rated fragment activity
                return new Favorite();
            case 1:
                // Games fragment activity
                return new HomeActivity();

        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }



}

