import React, { useState, useEffect } from 'react';
import axios from 'axios';

export default function App() {
  const [competitors, setCompetitors] = useState<any[]>([]);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [snapshots, setSnapshots] = useState<any[]>([]);
  const [analysis, setAnalysis] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [adding, setAdding] = useState(false);

  const [compName, setCompName] = useState('');
  const [compUrl, setCompUrl] = useState('');

  useEffect(() => {
    // 1. Fetch immediately when the dashboard loads
    fetchData();

    // 2. Set up the 10-second polling loop
    console.log("Starting 10-second live polling engine...");
    const interval = setInterval(() => {
      fetchData();
    }, 10000);

    // 3. Clean up the loop if the component ever unmounts
    return () => clearInterval(interval);
  }, []); // <-- The empty dependency array is crucial here

  const fetchData = async () => {
    try {
      const compRes = await axios.get('http://localhost:8080/api/competitors');
      setCompetitors(compRes.data);
      const notifRes = await axios.get('http://localhost:8080/api/data/notifications');
      setNotifications(notifRes.data);
      const snapRes = await axios.get('http://localhost:8080/api/data/snapshots');
      setSnapshots(snapRes.data);
    } catch (err) {
      console.error("Backend not reachable", err);
    }
  };

  const addCompetitor = async (e: React.FormEvent) => {
    e.preventDefault();
    setAdding(true);
    try {
      await axios.post('http://localhost:8080/api/competitors', {
        name: compName,
        url: compUrl
      });
      setCompName('');
      setCompUrl('');
      fetchData();
    } catch (err) {
      console.error(err);
      alert("Failed to add competitor. Ensure URL is perfectly valid (e.g., https://site.com)");
    }
    setAdding(false);
  };

  const triggerAnalysis = async () => {
    setLoading(true);
    try {
      const res = await axios.post('http://localhost:8080/api/analyze');
      setAnalysis(res.data);
      fetchData();
    } catch (err) {
      console.error(err);
    }
    setLoading(false);
  };

  const getCategoryStyles = (category: string) => {
    switch(category) {
      case 'PRICING': return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      case 'PRODUCT_LAUNCH': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'MESSAGING': return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      default: return 'bg-slate-500/10 text-slate-400 border-slate-500/30';
    }
  };

  return (
    <div className="flex h-screen bg-[#070b14] text-slate-200 font-sans overflow-hidden selection:bg-blue-500/30">
      {/* Sidebar */}
      <aside className="w-80 bg-[#0f172a]/80 backdrop-blur-2xl border-r border-slate-800 flex flex-col shadow-[4px_0_24px_rgba(0,0,0,0.5)] overflow-y-auto custom-scrollbar z-20">
        <div className="p-8 pb-4">
          <div className="text-2xl font-black tracking-tighter bg-gradient-to-br from-blue-400 via-indigo-400 to-emerald-400 bg-clip-text text-transparent drop-shadow-sm">
            SHERLOCK
          </div>
          <div className="text-[10px] font-bold text-slate-500 uppercase tracking-widest mt-1">Market Intelligence Node</div>
        </div>
        
        {/* Add Competitor Form */}
        <div className="px-6 py-4 border-b border-slate-800/80 mb-4">
           <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 flex items-center"><span className="mr-2 text-indigo-400">🎯</span> Track Entity</h3>
           <form onSubmit={addCompetitor} className="space-y-3">
              <input 
                 type="text" 
                 required
                 value={compName}
                 onChange={e => setCompName(e.target.value)}
                 className="w-full bg-[#070b14] border border-slate-700/50 rounded-lg px-4 py-2 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition-colors shadow-inner"
                 placeholder="Entity Name (e.g. Stripe)"
              />
              <input 
                 type="url" 
                 required
                 value={compUrl}
                 onChange={e => setCompUrl(e.target.value)}
                 className="w-full bg-[#070b14] border border-slate-700/50 rounded-lg px-4 py-2 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition-colors shadow-inner"
                 placeholder="URL (https://...)"
              />
              <button 
                 type="submit" 
                 disabled={adding}
                 className="w-full bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-500 hover:to-blue-500 disabled:opacity-50 text-white rounded-lg px-4 py-2.5 text-xs uppercase tracking-wider font-bold shadow-[0_0_15px_rgba(79,70,229,0.3)] transition-all border border-indigo-500/50"
              >
                 {adding ? "Deploying Scraper..." : "Initialize Tracking"}
              </button>
           </form>
        </div>

        {/* Competitor List */}
        <div className="px-6 pb-6 flex-1">
           <h3 className="text-[11px] font-bold uppercase tracking-widest text-slate-400 mb-4 flex items-center"><span className="mr-2 text-emerald-400">📡</span> Active Targets</h3>
           {competitors.length === 0 ? (
               <div className="text-center p-4 border border-slate-800 rounded-xl bg-slate-900/20">
                  <p className="text-xs text-slate-500">No active targets locked.</p>
               </div>
           ) : (
             <div className="space-y-2">
               {competitors.map((c, i) => (
                 <div key={i} className="group relative bg-[#0f172a]/60 p-3.5 rounded-xl border border-slate-800 hover:border-indigo-500/50 hover:bg-[#1e293b]/50 transition-all cursor-pointer">
                    <div className="absolute top-1/2 -left-[1px] -translate-y-1/2 w-1 h-0 bg-indigo-500 group-hover:h-6 transition-all rounded-r-lg shadow-[0_0_8px_rgba(99,102,241,0.8)]"></div>
                    <p className="text-sm font-semibold text-slate-200 truncate">{c.name}</p>
                    <a href={c.url} target="_blank" rel="noreferrer" className="text-[10px] text-slate-500 group-hover:text-indigo-400 truncate block mt-0.5 transition-colors">{c.url}</a>
                 </div>
               ))}
             </div>
           )}
        </div>
      </aside>

      {/* Main Panel */}
      <main className="flex-1 flex flex-col overflow-y-auto overflow-x-hidden relative">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-indigo-900/10 via-[#070b14]/0 to-transparent pointer-events-none"></div>
        
        <header className="sticky top-0 z-40 h-20 bg-[#070b14]/90 backdrop-blur-md border-b border-slate-800 flex items-center justify-between px-10">
          <h2 className="text-xl font-bold text-slate-200 drop-shadow-sm tracking-wide">Market Signals Dashboard</h2>
          <div className="flex items-center space-x-8">
             <button 
                onClick={triggerAnalysis} 
                disabled={loading || competitors.length === 0} 
                className="px-6 py-2.5 bg-slate-100 hover:bg-white text-slate-900 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg font-bold text-xs uppercase tracking-widest shadow-[0_0_20px_rgba(255,255,255,0.1)] transition-all outline-none"
             >
               {loading ? "Synthesizing Data..." : "Generate Insights"}
             </button>
          </div>
        </header>

        <div className="p-10 z-10 space-y-8 flex-1 flex flex-col max-w-7xl mx-auto w-full">
           {competitors.length === 0 ? (
              <div className="flex-1 flex flex-col items-center justify-center p-12 text-center w-full bg-[#0f172a]/30 border border-slate-800/80 rounded-2xl shadow-2xl backdrop-blur-sm">
                 <div className="w-16 h-16 bg-slate-800/50 rounded-2xl flex items-center justify-center mb-6 border border-slate-700/50 shadow-inner">
                    <span className="text-2xl opacity-50">📡</span>
                 </div>
                 <h2 className="text-xl font-bold text-slate-300 mb-2">Awaiting Target Parameters</h2>
                 <p className="text-slate-500 text-sm leading-relaxed mb-6 max-w-md">Input competitor endpoints in the command module to begin DOM tracking and semantic diffing.</p>
              </div>
           ) : (
             <>
               {/* Gemini Intelligence Panel */}
               {analysis && (
                  <div className="bg-[#0f172a]/80 backdrop-blur-xl rounded-2xl p-8 border border-slate-700/60 shadow-2xl relative overflow-hidden">
                     <div className="flex items-center gap-3 mb-8 border-b border-slate-800 pb-6">
                        <div className="p-2 bg-indigo-500/10 rounded-lg border border-indigo-500/20">
                           <span className="text-xl">🧠</span>
                        </div>
                        <div>
                           <h3 className="text-xl font-bold text-slate-100">Executive Summary</h3>
                           <p className="text-xs text-slate-400 mt-1">AI-Synthesized Market Shift Analysis</p>
                        </div>
                     </div>
                     
                     {/* Situational Analysis */}
                     <div className="mb-8">
                        <p className="text-slate-300 leading-relaxed font-medium text-sm md:text-base">{analysis.situational_analysis}</p>
                     </div>

                     {/* Granular Changes Array (The new schema!) */}
                     {analysis.specific_changes && analysis.specific_changes.length > 0 && (
                        <div className="mb-8">
                           <h4 className="text-[10px] uppercase tracking-widest text-slate-500 font-bold mb-4">Detected Micro-Shifts</h4>
                           <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                              {analysis.specific_changes.map((change: any, idx: number) => (
                                 <div key={idx} className="p-5 bg-[#070b14]/50 border border-slate-800 rounded-xl relative overflow-hidden">
                                    <div className={`inline-flex px-2 py-0.5 rounded text-[9px] font-black uppercase tracking-widest border mb-3 ${getCategoryStyles(change.category)}`}>
                                       {change.category}
                                    </div>
                                    <div className="space-y-2">
                                       <div>
                                          <p className="text-[10px] text-slate-500 uppercase tracking-wider mb-0.5">Previous State</p>
                                          <p className="text-xs text-slate-400 line-through decoration-rose-500/50">{change.old_state}</p>
                                       </div>
                                       <div>
                                          <p className="text-[10px] text-slate-500 uppercase tracking-wider mb-0.5">Current State</p>
                                          <p className="text-sm font-medium text-slate-200">{change.new_state}</p>
                                       </div>
                                    </div>
                                 </div>
                              ))}
                           </div>
                        </div>
                     )}

                     {/* Strategic Output */}
                     <div className="grid grid-cols-1 md:grid-cols-2 gap-6 relative z-10 pt-6 border-t border-slate-800">
                        <div className="space-y-2">
                           <h4 className="text-[11px] uppercase tracking-widest text-indigo-400 font-bold mb-2 flex items-center gap-2">
                              <span className="w-1.5 h-1.5 rounded-full bg-indigo-500"></span> Unclaimed Whitespace
                           </h4>
                           <p className="text-slate-400 leading-relaxed text-sm p-4 bg-[#070b14]/50 rounded-xl border border-slate-800/80">{analysis.whitespace}</p>
                        </div>
                        <div className="space-y-2">
                           <h4 className="text-[11px] uppercase tracking-widest text-emerald-400 font-bold mb-2 flex items-center gap-2">
                              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Counter-Strategy
                           </h4>
                           <p className="text-slate-400 leading-relaxed text-sm p-4 bg-[#070b14]/50 rounded-xl border border-slate-800/80">{analysis.strategy}</p>
                        </div>
                     </div>
                  </div>
               )}

               {/* Metrics Grid */}
               <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-4">
                  {/* Notifications List */}
                  <div className="bg-[#0f172a]/60 backdrop-blur-md rounded-2xl p-6 md:p-8 border border-slate-800 shadow-xl flex flex-col h-[450px]">
                     <h3 className="text-base font-bold mb-6 flex items-center justify-between text-slate-200">
                        Priority Alert Feed
                        <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest bg-slate-800/50 px-2 py-1 rounded">Automated</span>
                     </h3>
                     <div className="space-y-3 overflow-y-auto pr-2 custom-scrollbar flex-1">
                        {notifications.map((n: any, i: number) => (
                           <div key={i} className={`p-4 rounded-xl border ${n.score > 7 ? 'bg-rose-500/5 border-rose-500/20' : 'bg-[#070b14]/50 border-slate-800'} relative overflow-hidden flex flex-col`}>
                              <div className="flex justify-between items-center mb-2">
                                 <div className={`text-[9px] font-black uppercase tracking-widest px-2 py-0.5 rounded ${n.score > 7 ? 'bg-rose-500/10 text-rose-400' : 'bg-slate-700/50 text-slate-300'}`}>
                                    {n.impactLevel} IMPACT
                                 </div>
                                 <span className="text-[10px] text-slate-500 font-medium">{new Date(n.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                              </div>
                              <p className="text-sm font-medium text-slate-300 leading-snug break-words">{n.message}</p>
                           </div>
                        ))}
                        {notifications.length === 0 && <div className="h-full flex items-center justify-center"><p className="text-slate-500 text-xs uppercase tracking-widest">No active alerts</p></div>}
                     </div>
                  </div>

                  {/* Snapshot Timeline */}
                  <div className="bg-[#0f172a]/60 backdrop-blur-md rounded-2xl p-6 md:p-8 border border-slate-800 shadow-xl flex flex-col h-[450px]">
                     <h3 className="text-base font-bold mb-6 flex items-center justify-between text-slate-200">
                        System Sync Log
                        <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest bg-slate-800/50 px-2 py-1 rounded">Cron Engine</span>
                     </h3>
                     <div className="relative border-l border-slate-800 ml-3 space-y-6 overflow-y-auto pr-4 custom-scrollbar flex-1 pt-2">
                        {snapshots.map((s: any, i: number) => {
                           const cName = competitors.find(c => c.id === s.competitorId)?.name || "Unknown Entity";
                           return (
                             <div key={i} className="pl-6 relative">
                                <div className="absolute w-2.5 h-2.5 rounded-full bg-slate-400 border-[3px] border-[#0f172a] -left-[5.5px] top-1"></div>
                                <div className="text-[10px] text-slate-500 font-bold mb-1.5 tracking-wider uppercase">{new Date(s.timestamp).toLocaleString([], {dateStyle: 'short', timeStyle: 'short'})}</div>
                                <div className="p-3 bg-[#070b14]/50 rounded-lg border border-slate-800">
                                   <p className="text-xs font-semibold text-slate-200 mb-1">{cName}</p>
                                   <p className="text-[10px] text-slate-500 font-mono truncate">Hash: {s.contentHash}</p>
                                </div>
                             </div>
                           );
                        })}
                        {snapshots.length === 0 && <div className="h-full flex items-center justify-center ml-[-12px]"><p className="text-slate-500 text-xs uppercase tracking-widest">Awaiting data pull</p></div>}
                     </div>
                  </div>
               </div>
             </>
           )}
        </div>
      </main>
    </div>
  );
}