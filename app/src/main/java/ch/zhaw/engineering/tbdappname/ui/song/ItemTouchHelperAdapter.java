package ch.zhaw.engineering.tbdappname.ui.song;

/* package */ interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}