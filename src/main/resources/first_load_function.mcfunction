scoreboard objectives add structure_loaded dummy
scoreboard players add is_loaded structure_loaded 0
execute if score is_loaded structure_loaded matches 0 run function reload_all_structures
scoreboard players set is_loaded structure_loaded 1
