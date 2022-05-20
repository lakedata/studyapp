package ddwu.mobile.finalproject.ma02_20190999.todo;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ddwu.mobile.finalproject.ma02_20190999.R;
import ddwu.mobile.finalproject.ma02_20190999.data.ItemContract;

public class ItemCursorAdapter extends CursorAdapter {

    ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // 목록 항목 레이아웃에서 수정할 개별 보기 찾
        TextView nameTextView = view.findViewById(R.id.name_txt_view);
        TextView detailTextView = view.findViewById(R.id.detail_txt_view);
        TextView totalUnitTextView = view.findViewById(R.id.total_unit_txt_view);
        TextView unitTextView = view.findViewById(R.id.unit_txt_view);

        // 관심 있는 항목 속성의 열을 찾음
        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DETAIL);
        int totalUnitColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT);
        int unitColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_UNIT);

        // 커서에서 현재 항목의 항목 특성 읽기
        String itemName = cursor.getString(nameColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);
        int itemTotalUnit = cursor.getInt(totalUnitColumnIndex);
        int itemUnit = cursor.getInt(unitColumnIndex);

        String itemTotalUnitString = Integer.toString(itemTotalUnit);
        String itemUnitString = Integer.toString(itemUnit);

        // TextViews를 현재 항목의 특성으로 업데이트
        nameTextView.setText(itemName);
        detailTextView.setText(itemQuantity);
        totalUnitTextView.setText(itemTotalUnitString);
        unitTextView.setText(itemUnitString);

        // 항목 상태에 따라 이미지 리소스 변경
        int statusColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_STATUS);
        int statusOfItem = cursor.getInt(statusColumnIndex);

        ImageView statusImage = view.findViewById(R.id.item_status_imageview);

        switch (statusOfItem) {
            case 0:
                statusImage.setImageResource(R.drawable.ic_start_btn);
                break;
            case 1:
                statusImage.setImageResource(R.drawable.ic_stop_btn);
                break;
            case 2:
                statusImage.setImageResource(R.drawable.ic_done_btn);
                break;
        }
    }
}
