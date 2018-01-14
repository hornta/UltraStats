package me.amgf.UltraStats;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.UUID;

public class EntityDeathListener implements Listener {
    private ManagedData managedData;

    EntityDeathListener(ManagedData managedData) {
        this.managedData = managedData;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity() == null || e.getEntity().getKiller() == null) {
            return;
        }

        UUID playerUUID = e.getEntity().getKiller().getUniqueId();
        EntityType entityType = e.getEntityType();

        managedData.receiveEntityDeath(playerUUID, entityType);
    }
}
