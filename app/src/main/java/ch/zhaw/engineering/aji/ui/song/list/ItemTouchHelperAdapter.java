package ch.zhaw.engineering.aji.ui.song.list;

/* package */ interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}