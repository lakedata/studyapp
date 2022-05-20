package ddwu.mobile.finalproject.ma02_20190999.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ddwu.mobile.finalproject.ma02_20190999.R;
import ddwu.mobile.finalproject.ma02_20190999.library.libraryDto;

public class MyCustomAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private ArrayList<libraryDto> myDataList;
    private LayoutInflater layoutInflater;

    public MyCustomAdapter(Context context, int layout, ArrayList<libraryDto> myDataList) {
        this.context = context;
        this.layout = layout;
        this.myDataList = myDataList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return myDataList.size();
    }

    @Override
    public Object getItem(int pos) {
        return myDataList.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return myDataList.get(pos).get_id();
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        final int position = pos;

        if (view == null) {
            view = layoutInflater.inflate(layout, viewGroup, false);
        }

        TextView textLbrryNm = (TextView) view.findViewById(R.id.tvLbrryNm);
        TextView textCloseDay = (TextView) view.findViewById(R.id.tvCloseDay);
        TextView textRdnmadr = (TextView) view.findViewById(R.id.tvRdnmadr);

        textLbrryNm.setText(myDataList.get(pos).getLbrryNm());//도서명
        textCloseDay.setText(myDataList.get(pos).getCloseDay());//휴관일
        textRdnmadr.setText(myDataList.get(pos).getRdnmadr());//도로명

        return view;
    }

    public void setList(ArrayList<libraryDto> myDataList) {
        this.myDataList = myDataList;
    }
}
