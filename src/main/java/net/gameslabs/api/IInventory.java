package net.gameslabs.api;

import net.gameslabs.model.InventorySlot;

import java.util.ArrayList;

public interface IInventory {
    String getId();
    ArrayList<InventorySlot> getSlots();
    InventorySlot getSlot(int index);
}
