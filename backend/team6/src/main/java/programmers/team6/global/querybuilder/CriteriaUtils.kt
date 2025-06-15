package programmers.team6.global.querybuilder

import jakarta.persistence.criteria.From
import jakarta.persistence.criteria.Path
import jakarta.persistence.metamodel.SingularAttribute
import kotlin.text.get

/**
 * @author gunwoong
 */
object CriteriaUtils {
    /**
     * 연관관계가 있는 클래스들을 Root에서 연달아 찾아가야할 경우 위와같이 Path를 찾아냄
     * @param <X>
    </X> */
    fun <R> searchPath(root: From<R, *>, vararg fields: SingularAttribute<*, *>): Path<*> {
        return fields.fold(root as Path<*>) { path, field ->
            path.get(field as SingularAttribute<Any, Any>)
        }
    }

}
