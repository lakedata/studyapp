package ddwu.mobile.finalproject.ma02_20190999.todo;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
    private final static int ITEM_LIST = 0;
    private final static int ITEM_TIMER = 1;
    private final static int ITEM_STATISTICS = 2;
    private final static int ITEM_SETTING = 3;
    private final static int ITEM_ABOUT = 4;

    private int mNumbOfTabs;

    SimpleFragmentPagerAdapter(FragmentManager fm, int NumbOfTabs) {
        super(fm);
        this.mNumbOfTabs = NumbOfTabs;
    }

    public Fragment getItem(int position) {
        switch (position) {
            case ITEM_LIST:
                return new TodoListFragment();
            case ITEM_TIMER:
                return new TimerFragment();
            case ITEM_STATISTICS:
                //return new StatisticsFragment();
            case ITEM_SETTING:
                return new SettingFragment();
            case ITEM_ABOUT:
                //return new AboutFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumbOfTabs;
    }
}
