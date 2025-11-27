import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/task_provider.dart';
import '../providers/location_provider.dart';
import '../models/transport_task.dart';
import '../widgets/widgets.dart';

/// 任务列表屏幕
class TaskListScreen extends StatefulWidget {
  const TaskListScreen({super.key});

  @override
  State<TaskListScreen> createState() => _TaskListScreenState();
}

class _TaskListScreenState extends State<TaskListScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final ScrollController _scrollController = ScrollController();

  final List<TaskStatus?> _statusFilters = [
    null, // 全部
    TaskStatus.assigned,
    TaskStatus.inProgress,
    TaskStatus.completed,
    TaskStatus.abnormal,
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 5, vsync: this);
    _tabController.addListener(_onTabChanged);
    _scrollController.addListener(_onScroll);
    
    // 初始加载
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TaskProvider>().loadTasks();
    });
  }

  @override
  void dispose() {
    _tabController.removeListener(_onTabChanged);
    _tabController.dispose();
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    super.dispose();
  }

  void _onTabChanged() {
    if (_tabController.indexIsChanging) return;
    final status = _statusFilters[_tabController.index];
    context.read<TaskProvider>().setStatusFilter(status);
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      context.read<TaskProvider>().loadMoreTasks();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('运输任务'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: _showSearchDialog,
          ),
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: _showFilterSheet,
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          tabs: const [
            Tab(text: '全部'),
            Tab(text: '待开始'),
            Tab(text: '进行中'),
            Tab(text: '已完成'),
            Tab(text: '异常'),
          ],
        ),
      ),
      body: Consumer<TaskProvider>(
        builder: (context, taskProvider, child) {
          if (taskProvider.isLoading && taskProvider.tasks.isEmpty) {
            return ListView.builder(
              itemCount: 5,
              itemBuilder: (context, index) => const TaskCardSkeleton(),
            );
          }

          if (taskProvider.error != null && taskProvider.tasks.isEmpty) {
            return ErrorState(
              message: taskProvider.error,
              onRetry: () => taskProvider.loadTasks(),
            );
          }

          if (taskProvider.tasks.isEmpty) {
            return NoTasksState(
              onRefresh: () => taskProvider.loadTasks(),
            );
          }

          return RefreshIndicator(
            onRefresh: () => taskProvider.refreshTasks(),
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.symmetric(vertical: 8),
              itemCount: taskProvider.tasks.length + (taskProvider.hasMore ? 1 : 0),
              itemBuilder: (context, index) {
                if (index >= taskProvider.tasks.length) {
                  return const Padding(
                    padding: EdgeInsets.all(16),
                    child: Center(child: CircularProgressIndicator()),
                  );
                }

                final task = taskProvider.tasks[index];
                return TaskCard(
                  task: task,
                  showActions: true,
                  onTap: () => _navigateToDetail(task),
                  onStart: (t) => _startTask(t),
                  onComplete: (t) => _showCompleteDialog(t),
                  onReport: (t) => _navigateToReport(t),
                );
              },
            ),
          );
        },
      ),
      floatingActionButton: Consumer<LocationProvider>(
        builder: (context, locationProvider, child) {
          return FloatingActionButton.extended(
            onPressed: () => _toggleTracking(locationProvider),
            icon: Icon(
              locationProvider.isTracking
                  ? Icons.location_off
                  : Icons.location_on,
            ),
            label: Text(locationProvider.isTracking ? '停止定位' : '开始定位'),
            backgroundColor:
                locationProvider.isTracking ? Colors.red : Colors.blue,
          );
        },
      ),
    );
  }

  void _navigateToDetail(TransportTask task) {
    Navigator.pushNamed(context, '/task-detail', arguments: task.id);
  }

  void _navigateToReport(TransportTask task) {
    Navigator.pushNamed(context, '/report-anomaly', arguments: task.id);
  }

  Future<void> _startTask(TransportTask task) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('确认开始任务'),
        content: Text('确定要开始任务 ${task.taskNumber} 吗？'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('确定'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await context.read<TaskProvider>().startTask(task.id);
    }
  }

  Future<void> _showCompleteDialog(TransportTask task) async {
    final notesController = TextEditingController();
    
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('完成任务'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('确定要完成任务 ${task.taskNumber} 吗？'),
            const SizedBox(height: 16),
            TextField(
              controller: notesController,
              decoration: const InputDecoration(
                labelText: '备注 (可选)',
                border: OutlineInputBorder(),
              ),
              maxLines: 3,
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('完成'),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      await context.read<TaskProvider>().completeTask(
        task.id,
        notes: notesController.text.isEmpty ? null : notesController.text,
      );
    }

    notesController.dispose();
  }

  void _toggleTracking(LocationProvider locationProvider) {
    if (locationProvider.isTracking) {
      locationProvider.stopTracking();
    } else {
      locationProvider.startTracking();
    }
  }

  void _showSearchDialog() {
    showDialog(
      context: context,
      builder: (context) => _SearchDialog(
        onSearch: (query) {
          context.read<TaskProvider>().setSearchQuery(query);
        },
      ),
    );
  }

  void _showFilterSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => const _FilterSheet(),
    );
  }
}

/// 搜索对话框
class _SearchDialog extends StatefulWidget {
  final Function(String) onSearch;

  const _SearchDialog({required this.onSearch});

  @override
  State<_SearchDialog> createState() => _SearchDialogState();
}

class _SearchDialogState extends State<_SearchDialog> {
  final _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('搜索任务'),
      content: TextField(
        controller: _controller,
        autofocus: true,
        decoration: const InputDecoration(
          hintText: '输入任务编号或货物名称',
          prefixIcon: Icon(Icons.search),
          border: OutlineInputBorder(),
        ),
        onSubmitted: (value) {
          widget.onSearch(value);
          Navigator.pop(context);
        },
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('取消'),
        ),
        ElevatedButton(
          onPressed: () {
            widget.onSearch(_controller.text);
            Navigator.pop(context);
          },
          child: const Text('搜索'),
        ),
      ],
    );
  }
}

/// 筛选面板
class _FilterSheet extends StatefulWidget {
  const _FilterSheet();

  @override
  State<_FilterSheet> createState() => _FilterSheetState();
}

class _FilterSheetState extends State<_FilterSheet> {
  TaskPriority? _selectedPriority;
  DateTimeRange? _dateRange;

  @override
  Widget build(BuildContext context) {
    return DraggableScrollableSheet(
      initialChildSize: 0.5,
      minChildSize: 0.3,
      maxChildSize: 0.8,
      expand: false,
      builder: (context, scrollController) {
        return Container(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.grey[300],
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                '筛选条件',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              const Text('优先级'),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                children: [
                  FilterChip(
                    label: const Text('全部'),
                    selected: _selectedPriority == null,
                    onSelected: (_) => setState(() => _selectedPriority = null),
                  ),
                  ...TaskPriority.values.map((priority) => FilterChip(
                        label: Text(_getPriorityLabel(priority)),
                        selected: _selectedPriority == priority,
                        onSelected: (_) =>
                            setState(() => _selectedPriority = priority),
                      )),
                ],
              ),
              const SizedBox(height: 16),
              const Text('日期范围'),
              const SizedBox(height: 8),
              InkWell(
                onTap: _selectDateRange,
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.grey[300]!),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.calendar_today, size: 20),
                      const SizedBox(width: 8),
                      Text(
                        _dateRange != null
                            ? '${_formatDate(_dateRange!.start)} - ${_formatDate(_dateRange!.end)}'
                            : '选择日期范围',
                        style: TextStyle(
                          color: _dateRange != null
                              ? Colors.black
                              : Colors.grey[600],
                        ),
                      ),
                      const Spacer(),
                      if (_dateRange != null)
                        IconButton(
                          icon: const Icon(Icons.close, size: 20),
                          onPressed: () => setState(() => _dateRange = null),
                        ),
                    ],
                  ),
                ),
              ),
              const Spacer(),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () {
                        context.read<TaskProvider>().clearFilters();
                        Navigator.pop(context);
                      },
                      child: const Text('重置'),
                    ),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: ElevatedButton(
                      onPressed: () {
                        context.read<TaskProvider>().setPriorityFilter(_selectedPriority);
                        if (_dateRange != null) {
                          context.read<TaskProvider>().setDateRange(
                            _dateRange!.start,
                            _dateRange!.end,
                          );
                        }
                        Navigator.pop(context);
                      },
                      child: const Text('应用'),
                    ),
                  ),
                ],
              ),
            ],
          ),
        );
      },
    );
  }

  String _getPriorityLabel(TaskPriority priority) {
    switch (priority) {
      case TaskPriority.low:
        return '低';
      case TaskPriority.normal:
        return '普通';
      case TaskPriority.high:
        return '高';
      case TaskPriority.urgent:
        return '紧急';
    }
  }

  Future<void> _selectDateRange() async {
    final picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime.now().subtract(const Duration(days: 365)),
      lastDate: DateTime.now().add(const Duration(days: 365)),
      initialDateRange: _dateRange,
    );

    if (picked != null) {
      setState(() => _dateRange = picked);
    }
  }

  String _formatDate(DateTime date) {
    return '${date.month}/${date.day}';
  }
}
