package com.skillcraft.repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
	T findByEmail(String email);
	T findByLogin(String login);
	List<T> findByIdIn(Collection<ID> ids);
}
