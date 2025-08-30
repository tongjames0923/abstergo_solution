select de.id                                                                               as id,
       de.department_name                                                                  as department_name,
       de.description                                                                      as description,
       de.flag                                                                             as flag,
       de.parent_id                                                                        as parent_id,
       de.sort_order                                                                       as sort_order,
       de.update_time                                                                      as update_time,
       de.create_time                                                                      as create_time
 from sys_department de
