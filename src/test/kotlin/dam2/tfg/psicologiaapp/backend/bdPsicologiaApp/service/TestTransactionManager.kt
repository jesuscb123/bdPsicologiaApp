package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus

/** Transaction manager mínimo para tests unitarios que usan [org.springframework.transaction.support.TransactionTemplate]. */
internal class TestTransactionManager : AbstractPlatformTransactionManager() {
    override fun doGetTransaction(): Any = Object()
    override fun doBegin(transaction: Any, definition: TransactionDefinition) {}
    override fun doCommit(status: DefaultTransactionStatus) {}
    override fun doRollback(status: DefaultTransactionStatus) {}
}
