select count(x.class_cd) from (
  select distinct(class_cd) as class_cd from orders where domain_id = :domainId and batch_id = :mergeBatchId
  except
  select distinct(class_cd) as class_cd from orders where domain_id = :domainId and batch_id = :batchId
) x