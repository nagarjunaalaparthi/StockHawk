package com.stockhawk.adapter;

/**
 * Interface for enabling swiping to delete
 */
public interface ItemTouchHelperViewHolder {
  void onItemSelected();

  void onItemClear();
}
