package com.badoo.reaktive.scheduler

/**
 * Provides the global instance of Main [Scheduler]
 */
val mainScheduler: Scheduler get() = mainSchedulerFactory.value

/**
 * Provides the global instance of Computation [Scheduler]
 */
val computationScheduler: Scheduler get() = computationSchedulerFactory.value

/**
 * Provides the global instance of IO [Scheduler]
 */
val ioScheduler: Scheduler get() = ioSchedulerFactory.value

/**
 * Provides the global instance of Trampoline [Scheduler]
 */
val trampolineScheduler: Scheduler get() = trampolineSchedulerFactory.value

/**
 * Provides the global instance of Single [Scheduler]
 */
val singleScheduler: Scheduler get() = singleSchedulerFactory.value

/**
 * Provides the global instance of New Thread [Scheduler]
 */
val newThreadScheduler: Scheduler get() = newThreadSchedulerFactory.value

private var mainSchedulerFactory: Lazy<Scheduler> =
    lazy(::createMainScheduler)

private var computationSchedulerFactory: Lazy<Scheduler> =
    lazy(::createComputationScheduler)

private var ioSchedulerFactory: Lazy<Scheduler> =
    lazy(::createIoScheduler)

private var trampolineSchedulerFactory: Lazy<Scheduler> =
    lazy(::createTrampolineScheduler)

private var singleSchedulerFactory: Lazy<Scheduler> =
    lazy(::createSingleScheduler)

private var newThreadSchedulerFactory: Lazy<Scheduler> =
    lazy(::createNewThreadScheduler)

/**
 * Creates a new instance of Main [Scheduler]
 */
expect fun createMainScheduler(): Scheduler

/**
 * Creates a new instance of IO [Scheduler]
 */
expect fun createIoScheduler(): Scheduler

/**
 * Creates a new instance of Computation [Scheduler]
 */
expect fun createComputationScheduler(): Scheduler

/**
 * Creates a new instance of Trampoline [Scheduler]
 */
expect fun createTrampolineScheduler(): Scheduler

/**
 * Creates a new instance of Trampoline [Scheduler]
 */
expect fun createSingleScheduler(): Scheduler

/**
 * Creates a new instance of New Thread [Scheduler]
 */
expect fun createNewThreadScheduler(): Scheduler

/**
 * Overrides [Scheduler]s if they were not created yet
 *
 * @param main a factory for Main [Scheduler], if not set then default factory will be used
 * @param computation a factory for Computation [Scheduler], if not set then default factory will be used
 * @param io a factory for IO [Scheduler], if not set then default factory will be used
 * @param trampoline a factory for Trampoline [Scheduler], if not set then default factory will be used
 * @param single a factory for Single [Scheduler], if not set then default factory will be used
 * @param newThread a factory for New Thread [Scheduler], if not set then default factory will be used
 */
fun overrideSchedulers(
    main: () -> Scheduler = ::createMainScheduler,
    computation: () -> Scheduler = ::createComputationScheduler,
    io: () -> Scheduler = ::createIoScheduler,
    trampoline: () -> Scheduler = ::createTrampolineScheduler,
    single: () -> Scheduler = ::createSingleScheduler,
    newThread: () -> Scheduler = ::createNewThreadScheduler
) {
    mainSchedulerFactory = lazy(main)
    computationSchedulerFactory = lazy(computation)
    ioSchedulerFactory = lazy(io)
    trampolineSchedulerFactory = lazy(trampoline)
    singleSchedulerFactory = lazy(single)
    newThreadSchedulerFactory = lazy(newThread)
}
