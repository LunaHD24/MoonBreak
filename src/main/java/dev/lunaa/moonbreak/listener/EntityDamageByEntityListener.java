package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class EntityDamageByEntityListener implements Listener {

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof LivingEntity livingEntity)) return;
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) return;

        ItemStack item = equipment.getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        Optional<CustomTool> optionalTool = CustomTool.from(item);
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(CustomToolType.EventHook.ENTITY_DAMAGE_BY_ENTITY, e, tool);
    }

}
