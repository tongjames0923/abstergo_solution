SELECT *
FROM (SELECT d.*,
             CASE WHEN n.parent_id IS NULL THEN 1 ELSE 0 END AS tree_root_flag
      FROM sys_company d
               LEFT JOIN tree_node n ON n.entity_id = d.id AND n.group_name LIKE 'COT:%') AS temp
