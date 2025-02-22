package io.gitlab.arturbosch.detekt.rules.naming

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.config
import io.gitlab.arturbosch.detekt.api.internal.ActiveByDefault
import io.gitlab.arturbosch.detekt.api.internal.Configuration
import io.gitlab.arturbosch.detekt.rules.isOverride
import io.gitlab.arturbosch.detekt.rules.naming.util.isContainingExcludedClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Reports function names that do not follow the specified naming convention.
 * One exception are factory functions used to create instances of classes.
 * These factory functions can have the same name as the class being created.
 */
@ActiveByDefault(since = "1.0.0")
class FunctionNaming(config: Config = Config.empty) : Rule(config) {

    override val defaultRuleIdAliases: Set<String> = setOf("FunctionName")

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Function names should follow the naming convention set in the configuration.",
        debt = Debt.FIVE_MINS
    )

    @Configuration("naming pattern")
    private val functionPattern: Regex by config("[a-z][a-zA-Z0-9]*", String::toRegex)

    @Configuration("ignores functions in classes which match this regex")
    private val excludeClassPattern: Regex by config("$^", String::toRegex)

    @Configuration("ignores functions that have the override modifier")
    private val ignoreOverridden: Boolean by config(true)

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)

        if (ignoreOverridden && function.isOverride()) {
            return
        }

        val functionName = function.nameIdentifier?.text ?: return
        if (!function.isContainingExcludedClassOrObject(excludeClassPattern) &&
            !functionName.matches(functionPattern) &&
            functionName != function.typeReference?.text
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.atName(function),
                    message = "Function names should match the pattern: $functionPattern"
                )
            )
        }
    }

    companion object {
        const val FUNCTION_PATTERN = "functionPattern"
        const val EXCLUDE_CLASS_PATTERN = "excludeClassPattern"
        const val IGNORE_OVERRIDDEN = "ignoreOverridden"
    }
}
