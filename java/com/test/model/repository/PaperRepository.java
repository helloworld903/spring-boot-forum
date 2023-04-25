package com.test.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.test.model.entity.Paper;

@Repository("papers")
public interface PaperRepository extends JpaRepository<Paper, Long>{
	Optional<Paper> findById(Long id);
	Paper findByTitleAndAuthorName(String title,String name);
	
	@Query(value="select * from papers order by commentCount desc limit ?1,?2",nativeQuery=true)
	public List<Paper>findBatchPaperByCommentCount(Integer offset,Integer count);
	@Query(value="select * from papers where tag=?1 order by commentCount desc limit ?2,?3",nativeQuery=true)
	public List<Paper>findBatchPaperByCommentCountAndType(String type,Integer offset,Integer count);
	
	@Query(value="select * from papers order by id desc limit ?1,?2",nativeQuery=true)
	public List<Paper>findBatchPaper(Integer offset,Integer count);
	@Query(value="select * from papers where tag=?1 order by id desc limit ?2,?3",nativeQuery=true)
	public List<Paper>findBatchPaperAndType(String type,Integer offset,Integer count);
	
	@Query(value="select * from papers where to_days(now())- to_days(publishDate)<7 order by commentCount desc limit 0,10",nativeQuery=true)
	public List<Paper>findBatchPaperByCommentCountWhthinWeek();
}
