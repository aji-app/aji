package ch.zhaw.engineering.tbdappname.ui.songsold;

public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}