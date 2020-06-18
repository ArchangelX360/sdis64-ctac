package fr.sdis64.brain.utilities

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface SetCrudRepository<T, ID> : CrudRepository<T, ID> {
    override fun findAll(): Set<T>
}
