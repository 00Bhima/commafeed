package com.commafeed.backend.dao;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.commafeed.backend.model.AbstractModel;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.reflect.TypeToken;
import com.uaihebert.factory.EasyCriteriaFactory;
import com.uaihebert.model.EasyCriteria;

@SuppressWarnings("serial")
public abstract class GenericDAO<T, K> implements
		Serializable {

	private TypeToken<T> type = new TypeToken<T>(getClass()) {
	};

	@PersistenceContext
	protected EntityManager em;

	protected CriteriaBuilder builder;

	@PostConstruct
	public void init() {
		builder = em.getCriteriaBuilder();
	}

	public void save(T object) {
		em.persist(object);
	}

	public void update(T... objects) {
		for (Object object : objects) {
			em.merge(object);
		}
	}

	public void saveOrUpdate(AbstractModel m) {
		if (m.getId() == null) {
			em.persist(m);
		} else {
			em.merge(m);
		}
	}

	public void delete(T object) {
		object = em.merge(object);
		em.remove(object);
	}

	public void deleteById(K id) {
		Object ref = em.getReference(getType(), id);
		em.remove(ref);
	}

	public T findById(K id) {
		T t = em.find(getType(), id);
		return t;
	}

	public List<T> findAll() {
		return EasyCriteriaFactory.createQueryCriteria(em, getType())
				.getResultList();
	}

	public List<T> findAll(int startIndex, int count) {
		EasyCriteria<T> criteria = EasyCriteriaFactory.createQueryCriteria(em,
				getType());
		criteria.setMaxResults(count);
		criteria.setFirstResult(startIndex);
		return criteria.getResultList();
	}

	public long getCount() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<T> root = query.from(getType());
		query.select(builder.count(root));
		return em.createQuery(query).getSingleResult();
	}

	public List<T> findByField(String field, Object value) {
		EasyCriteria<T> criteria = EasyCriteriaFactory.createQueryCriteria(em,
				getType());
		criteria.andEquals(field, value);
		return criteria.getResultList();
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getType() {
		return (Class<T>) type.getRawType();
	}
	
	public EasyCriteria<T> createCriteria(){
		return EasyCriteriaFactory.createQueryCriteria(em, getType());
	}

	protected T proxy() {
		return MF.p(getType());
	}

}
