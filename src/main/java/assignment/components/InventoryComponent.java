package assignment.components;

import net.gameslabs.api.Component;
import net.gameslabs.api.IInventory;
import net.gameslabs.events.DropAllItemsEvent;
import net.gameslabs.events.DropItemEvent;
import net.gameslabs.events.PickupItemEvent;
import net.gameslabs.model.InventorySlot;
import net.gameslabs.model.Item;

import java.util.ArrayList;

public class InventoryComponent extends Component implements IInventory {
    private ArrayList<InventorySlot> slots;
    private final int numSlots;
    private final int slotSize;

    public InventoryComponent(int numSlots, int slotSize) {
        this.numSlots = numSlots;
        this.slotSize = slotSize;
        this.slots = createEmptySlots();
    }

    @Override
    public void onLoad() {
        registerEvent(DropItemEvent.class, this::onDropItem);
        registerEvent(PickupItemEvent.class, this::onPickupItem);
        registerEvent(DropAllItemsEvent.class, this::onDropAllItems);
    }

    @Override
    public void onUnload() {
    }

    @Override
    public String toString() {
        String out = "\r\nInventory (Size: " + this.slots.size() + ")\r\n";

        for (int i = 0; i < this.slots.size(); i++) {
            out += "Slot " + i + ": " + this.slots.get(i) + "\r\n";
        }

        return out;
    }

    public ArrayList<InventorySlot> getSlots() {
        return this.slots;
    }

    public InventorySlot getSlot(int index) {
        return this.slots.get(index);
    }

    private void onDropItem(DropItemEvent event) {
        if (event == null || event.getItem() == null || event.getCount() <= 0) {
            event.setCancelled(true);
            return;
        }

        int numToDrop = event.getCount();
        while (numToDrop > 0) {
            InventorySlot slot = tryGetSlotContainingItem(event.getItem());
            if (slot == null) {
                event.setCancelled(true);
                return;
            }

            if (numToDrop > slot.getCount()) {
                numToDrop -= slot.getCount();
                slot.drop(slot.getCount());
            } else {
                slot.drop(numToDrop);
                numToDrop = 0;
            }
        }
    }

    private void onPickupItem(PickupItemEvent event) {
        if (event == null || event.getItem() == null || event.getCount() <= 0) {
            event.setCancelled(true);
            return;
        }

        int numToAdd = event.getCount();
        while (numToAdd > 0) {
            InventorySlot slot = tryGetCompatibleSlot(event.getItem());
            if (slot == null) {
                event.setCancelled(true);
                return;
            }

            boolean success = false;
            if (numToAdd > slot.remainingCapacity()) {
                numToAdd -= slot.remainingCapacity();
                success = slot.tryAddingItem(event.getItem(), slot.remainingCapacity());
            } else {
                success = slot.tryAddingItem(event.getItem(), numToAdd);
                numToAdd = 0;
            }

            if (!success) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void onDropAllItems(DropAllItemsEvent event) {
        this.slots = createEmptySlots();
    }

    private ArrayList<InventorySlot> createEmptySlots() {
        ArrayList<InventorySlot> emptySlots = new ArrayList<InventorySlot>();
        for (int i = 0; i < numSlots; i++) {
            emptySlots.add(new InventorySlot(slotSize));
        }
        return emptySlots;
    }

    private InventorySlot tryGetSlotContainingItem(Item item) {
        for (int i = 0; i < slots.size(); i++) {
            InventorySlot slot = slots.get(i);

            if (slot.hasItem(item)) {
                return slot;
            }
        }

        return null;
    }

    private InventorySlot tryGetCompatibleSlot(Item item) {
        //Check for a non-full slot matching the item
        for (int i = 0; i < slots.size(); i++) {
            InventorySlot slot = slots.get(i);
            if (!slot.isEmpty() && slot.remainingCapacity() > 0 && slot.getItem().equals(item)) {
                return slot;
            }
        }

        //Check for an empty slot
        for (int i = 0; i < slots.size(); i++) {
            InventorySlot slot = slots.get(i);
            if (slot.isEmpty()) {
                return slot;
            }
        }

        return null;
    }
}