package ch.zhaw.engineering.tbdappname.ui.songs;

public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}