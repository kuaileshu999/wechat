import { useState, useEffect } from 'react';
import { 
  Calendar, 
  Clock, 
  CheckCircle2, 
  XCircle, 
  ChevronRight, 
  MessageSquare, 
  Sparkles, 
  Gift, 
  AlertCircle,
  Star,
  Users,
  Trophy,
  ArrowRight,
  ShieldCheck,
  PlayCircle
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs) {
  return twMerge(clsx(inputs));
}

const COURSES = [
  { date: '5月1日', time: '19:00 - 20:00', title: '计算图形面积 1', tag: '入门', icon: '📐' },
  { date: '5月2日', time: '19:00 - 20:00', title: '计算图形面积 2', tag: '进阶', icon: '📊' },
  { date: '5月3日', time: '19:00 - 20:00', title: '计算图形面积 3', tag: '提分', icon: '🚀' },
  { date: '5月4日', time: '19:00 - 20:00', title: '计算图形面积 4', tag: '避坑', icon: '🛡️' },
  { date: '5月5日', time: '19:00 - 20:00', title: '计算图形面积 5', tag: '实战', icon: '🏆' },
];

const FEATURES = [
  { icon: <Star className="text-yellow-500" />, title: '名师直播', desc: '清北名师在线授课' },
  { icon: <Users className="text-blue-500" />, title: '社群答疑', desc: '专业辅导老师陪伴' },
  { icon: <Trophy className="text-orange-500" />, title: '结业好礼', desc: '完课领取实体礼包' },
];

export default function App() {
  const [view, setView] = useState('landing'); // 'landing', 'select', 'confirmed', 'leave_result'
  const [showLeaveModal, setShowLeaveModal] = useState(false);
  const [leaveReason, setLeaveReason] = useState('');
  const [submittedLeaveReason, setSubmittedLeaveReason] = useState('');

  // Scroll to top on view change
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [view]);

  const handleStart = () => setView('select');
  const handleConfirmAttendance = () => setView('confirmed');
  const handleOpenLeaveModal = () => setShowLeaveModal(true);
  
  const handleConfirmLeave = () => {
    if (!leaveReason.trim()) {
      alert('请选择无法上课的理由，我们会为您推荐合适的上课方式');
      return;
    }
    setSubmittedLeaveReason(leaveReason);
    setShowLeaveModal(false);
    setView('leave_result');
  };

  const handleCancelLeave = () => {
    setShowLeaveModal(false);
  };

  return (
    <div className="min-h-screen bg-[#FDFEFF] text-slate-900 font-sans selection:bg-blue-100 overflow-x-hidden">
      <AnimatePresence mode="wait">
        {view === 'landing' && (
          <motion.div
            key="landing"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0, y: -20 }}
            className="flex flex-col min-h-screen"
          >
            {/* Hero Section */}
            <div className="relative pt-12 pb-20 px-6 overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-[600px] bg-gradient-to-br from-blue-600 via-indigo-600 to-violet-700 -z-10 rounded-b-[40px]" />
              <div className="absolute top-20 right-[-10%] w-64 h-64 bg-white/10 rounded-full blur-3xl" />
              <div className="absolute bottom-20 left-[-10%] w-64 h-64 bg-blue-400/20 rounded-full blur-3xl" />
              
              <div className="max-w-md mx-auto text-center text-white space-y-6">
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="inline-flex items-center gap-2 px-4 py-1.5 bg-white/20 backdrop-blur-md rounded-full text-xs font-bold border border-white/30"
                >
                  <Sparkles size={14} className="text-yellow-300" />
                  <span>2026 假期特别企划</span>
                </motion.div>
                
                <motion.h1
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.1 }}
                  className="text-5xl font-black tracking-tight leading-[1.1]"
                >
                  五一数学<br />
                  <span className="text-transparent bg-clip-text bg-gradient-to-r from-yellow-200 to-yellow-500">
                    高分特训营
                  </span>
                </motion.h1>
                
                <motion.p
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2 }}
                  className="text-blue-100 text-lg font-medium opacity-90"
                >
                  5天攻克图形面积，掌握解题精髓<br />助力期末冲刺，赢取精美大礼
                </motion.p>

                <motion.div
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: 0.3 }}
                  className="relative pt-8"
                >
                  <div className="absolute inset-0 bg-yellow-400 blur-2xl opacity-20 animate-pulse" />
                  <button
                    onClick={handleStart}
                    className="relative w-full py-5 bg-yellow-400 hover:bg-yellow-300 text-blue-900 rounded-2xl font-black text-xl shadow-[0_10px_30px_rgba(234,179,8,0.3)] transition-all active:scale-95 flex items-center justify-center gap-2"
                  >
                    立即开启特训
                    <ArrowRight size={24} />
                  </button>
                </motion.div>
              </div>
            </div>

            {/* Features Grid */}
            <div className="max-w-md mx-auto px-6 -mt-10 mb-12">
              <div className="grid grid-cols-3 gap-3">
                {FEATURES.map((feat, i) => (
                  <motion.div
                    key={i}
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.4 + i * 0.1 }}
                    className="bg-white p-4 rounded-2xl shadow-xl shadow-slate-200/50 flex flex-col items-center text-center gap-2 border border-slate-50"
                  >
                    <div className="p-2 bg-slate-50 rounded-xl">
                      {feat.icon}
                    </div>
                    <div className="text-[10px] font-black text-slate-800 uppercase tracking-wider">{feat.title}</div>
                    <div className="text-[9px] text-slate-400 font-medium leading-tight">{feat.desc}</div>
                  </motion.div>
                ))}
              </div>
            </div>

            {/* Course Preview */}
            <div className="max-w-md mx-auto px-6 pb-20 space-y-6">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-black text-slate-800">特训课程大纲</h2>
                <div className="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-[10px] font-bold uppercase tracking-wider">
                  5 Lessons
                </div>
              </div>
              
              <div className="space-y-4">
                {COURSES.map((course, i) => (
                  <motion.div
                    key={i}
                    initial={{ opacity: 0, x: -20 }}
                    whileInView={{ opacity: 1, x: 0 }}
                    viewport={{ once: true }}
                    transition={{ delay: i * 0.1 }}
                    className="group relative bg-white p-5 rounded-3xl border border-slate-100 shadow-sm hover:shadow-md transition-all"
                  >
                    <div className="flex items-center gap-4">
                      <div className="flex flex-col items-center justify-center w-14 h-14 bg-slate-50 rounded-2xl group-hover:bg-blue-50 transition-colors">
                        <span className="text-2xl mb-1">{course.icon}</span>
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="px-2 py-0.5 bg-indigo-50 text-indigo-600 text-[9px] font-bold rounded-md uppercase">
                            {course.tag}
                          </span>
                          <span className="text-xs font-bold text-slate-400">{course.date}</span>
                        </div>
                        <h3 className="text-base font-black text-slate-800">{course.title}</h3>
                      </div>
                      <ChevronRight size={20} className="text-slate-300 group-hover:text-blue-500 transition-colors" />
                    </div>
                  </motion.div>
                ))}
              </div>

              {/* Reward Banner */}
              <div className="p-6 bg-gradient-to-br from-orange-50 to-yellow-50 rounded-[32px] border border-orange-100 relative overflow-hidden group">
                <div className="absolute top-[-20px] right-[-20px] w-24 h-24 bg-orange-200/20 rounded-full blur-2xl group-hover:scale-150 transition-transform duration-700" />
                <div className="flex gap-4 relative z-10">
                  <div className="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-orange-500 shadow-sm">
                    <Gift size={24} />
                  </div>
                  <div className="flex-1">
                    <h4 className="text-sm font-black text-orange-900 mb-1 uppercase tracking-wider">Attendance Rewards</h4>
                    <p className="text-xs text-orange-800/70 leading-relaxed font-medium">
                      连续 5 天打卡，即可领取<span className="text-orange-600 font-bold">《小学几何提分秘籍》</span>及限量定制文具礼盒！
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Sticky Footer */}
            <div className="fixed bottom-0 left-0 w-full p-6 bg-white/80 backdrop-blur-xl border-t border-slate-100 z-50">
              <div className="max-w-md mx-auto">
                <button
                  onClick={handleStart}
                  className="w-full py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl font-bold text-lg shadow-lg shadow-blue-200 transition-all active:scale-95 flex items-center justify-center gap-2"
                >
                  立即参与锁定名额
                  <ChevronRight size={20} />
                </button>
              </div>
            </div>
          </motion.div>
        )}

        {view === 'select' && (
          <motion.div
            key="select"
            initial={{ opacity: 0, x: 100 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -100 }}
            className="p-6 max-w-md mx-auto space-y-8"
          >
            <div className="space-y-2">
              <button 
                onClick={() => setView('landing')}
                className="p-2 -ml-2 text-slate-400 hover:text-slate-600"
              >
                <ChevronRight className="rotate-180" size={24} />
              </button>
              <h2 className="text-3xl font-black text-slate-900">确认到课意愿</h2>
              <p className="text-slate-500 font-medium">请确认您在特训营期间的出席情况</p>
            </div>

            <div className="bg-slate-50 p-6 rounded-[32px] border border-slate-100 space-y-6">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-blue-600 shadow-sm">
                  <Clock size={20} />
                </div>
                <div>
                  <div className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Training Time</div>
                  <div className="text-sm font-bold text-slate-800">5月1日 - 5月5日 19:00-20:00</div>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between px-1">
                  <span className="text-xs font-black text-slate-400 uppercase tracking-wider">Quick Select</span>
                </div>
                <div className="grid gap-4">
                  <button
                    onClick={handleConfirmAttendance}
                    className="group relative p-6 bg-white rounded-3xl border-2 border-transparent hover:border-blue-500 shadow-sm transition-all text-left"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
                        <CheckCircle2 size={24} />
                      </div>
                      <div className="px-2 py-0.5 bg-green-50 text-green-600 text-[10px] font-bold rounded uppercase">Recommended</div>
                    </div>
                    <h3 className="text-lg font-black text-slate-800">确认准时到课</h3>
                    <p className="text-xs text-slate-400 font-medium mt-1">坚持学习，领取全勤大礼包</p>
                  </button>

                  <button
                    onClick={handleOpenLeaveModal}
                    className="p-6 bg-white rounded-3xl border-2 border-transparent hover:border-slate-200 shadow-sm transition-all text-left"
                  >
                    <div className="w-10 h-10 bg-slate-50 rounded-xl flex items-center justify-center text-slate-400 mb-2">
                      <XCircle size={24} />
                    </div>
                    <h3 className="text-lg font-black text-slate-800">暂时无法参加</h3>
                    <p className="text-xs text-slate-400 font-medium mt-1">我们会为您保留课程回放</p>
                  </button>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-2xl border border-blue-100">
              <ShieldCheck className="text-blue-500" size={20} />
              <p className="text-[11px] text-blue-800/70 font-medium leading-relaxed">
                您的席位将为您保留 24 小时，请尽快确认意愿。
              </p>
            </div>
          </motion.div>
        )}

        {view === 'confirmed' && (
          <motion.div
            key="confirmed"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="p-6 max-w-md mx-auto flex flex-col items-center min-h-[80vh] justify-center text-center space-y-8"
          >
            <div className="relative">
              <motion.div
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ type: "spring", damping: 12 }}
                className="w-32 h-32 bg-green-500 rounded-[40px] flex items-center justify-center text-white shadow-2xl shadow-green-200 relative z-10"
              >
                <CheckCircle2 size={64} strokeWidth={2.5} />
              </motion.div>
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ repeat: Infinity, duration: 20, ease: "linear" }}
                className="absolute inset-[-20px] border-2 border-dashed border-green-200 rounded-full"
              />
              <motion.div
                animate={{ scale: [1, 1.5, 1], opacity: [0.5, 0.2, 0.5] }}
                transition={{ repeat: Infinity, duration: 3 }}
                className="absolute inset-0 bg-green-400 rounded-full blur-3xl -z-10"
              />
            </div>

            <div className="space-y-2">
              <h2 className="text-3xl font-black text-slate-900">锁定席位成功</h2>
              <p className="text-slate-500 font-medium">
                恭喜！您已成功加入五一特训营<br />助教老师将在 24 小时内联系您
              </p>
            </div>

            <div className="w-full bg-slate-50 p-6 rounded-[32px] border border-slate-100 space-y-4">
              <div className="text-xs font-black text-slate-400 uppercase tracking-widest">Next Steps</div>
              <div className="space-y-3">
                {[
                  { icon: <PlayCircle size={16} />, text: '查看课前预习资料' },
                  { icon: <MessageSquare size={16} />, text: '进入专属学习社群' },
                  { icon: <Calendar size={16} />, text: '添加上课闹钟提醒' },
                ].map((item, i) => (
                  <div key={i} className="flex items-center gap-3 p-3 bg-white rounded-2xl border border-slate-100 text-sm font-bold text-slate-700">
                    <div className="text-blue-500">{item.icon}</div>
                    {item.text}
                  </div>
                ))}
              </div>
            </div>

            <button
              onClick={() => setView('landing')}
              className="px-8 py-3 bg-slate-900 text-white rounded-2xl font-bold transition-all active:scale-95"
            >
              返回首页
            </button>
          </motion.div>
        )}

        {view === 'leave_result' && (
          <motion.div
            key="leave"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="p-6 max-w-md mx-auto space-y-8 pt-12"
          >
            <div className="flex flex-col items-center text-center space-y-4">
              <div className="w-24 h-24 bg-orange-50 rounded-[32px] flex items-center justify-center text-orange-500">
                <AlertCircle size={48} />
              </div>
              <div className="space-y-2">
                <h2 className="text-3xl font-black text-slate-900">请假申请已记录</h2>
                <p className="text-slate-500 font-medium">很遗憾您无法准时参加本次特训</p>
              </div>
            </div>

            <div className="bg-slate-900 text-white p-8 rounded-[40px] shadow-2xl relative overflow-hidden">
              <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -mr-16 -mt-16 blur-2xl" />
              <div className="relative z-10 space-y-6">
                <div>
                  <div className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Status</div>
                  <div className="text-lg font-bold">已安排课程回放</div>
                </div>
                <div>
                  <div className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Reason</div>
                  <div className="text-sm font-medium opacity-80 italic">“ {submittedLeaveReason} ”</div>
                </div>
                <div className="pt-4 border-t border-white/10">
                  <p className="text-xs font-medium opacity-60 leading-relaxed">
                    虽然无法参加直播，但我们依然为您保留了学习资料的获取权限。
                  </p>
                </div>
              </div>
            </div>

            <button
              onClick={() => setView('select')}
              className="w-full py-4 text-slate-400 font-bold hover:text-slate-600 transition-colors"
            >
              重新选择上课意愿
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Leave Reason Modal */}
      <AnimatePresence>
        {showLeaveModal && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={handleCancelLeave}
              className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[100]"
            />
            <motion.div
              initial={{ opacity: 0, y: 100 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 100 }}
              className="fixed bottom-0 left-0 w-full bg-white rounded-t-[40px] p-8 z-[101] max-w-md mx-auto left-1/2 -translate-x-1/2"
            >
              <div className="w-12 h-1.5 bg-slate-100 rounded-full mx-auto mb-8" />
              <div className="space-y-6">
                <div>
                  <h3 className="text-2xl font-black text-slate-900">请假理由</h3>
                  <p className="text-slate-500 text-sm font-medium mt-1">选择理由，我们将为您推荐补课方案</p>
                </div>
                
                <div className="grid gap-3">
                  {['时间冲突', '身体不适', '外出旅游', '其他原因'].map((reason) => (
                    <button
                      key={reason}
                      onClick={() => setLeaveReason(reason)}
                      className={cn(
                        "w-full p-5 rounded-2xl text-left font-bold transition-all border-2",
                        leaveReason === reason 
                          ? "bg-blue-50 border-blue-500 text-blue-700 shadow-sm shadow-blue-100" 
                          : "bg-slate-50 border-transparent text-slate-600 hover:bg-slate-100"
                      )}
                    >
                      {reason}
                    </button>
                  ))}
                </div>

                <div className="flex gap-4 pt-4">
                  <button
                    onClick={handleCancelLeave}
                    className="flex-1 py-4 bg-slate-100 text-slate-600 rounded-2xl font-bold active:scale-95 transition-all"
                  >
                    取消
                  </button>
                  <button
                    onClick={handleConfirmLeave}
                    className="flex-2 py-4 bg-blue-600 text-white rounded-2xl font-bold active:scale-95 transition-all shadow-lg shadow-blue-200"
                  >
                    确认请假
                  </button>
                </div>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}
