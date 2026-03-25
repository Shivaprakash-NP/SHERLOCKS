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
    fetchData();
  }, []);

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

  return (
    <div className="flex h-screen bg-[#0f172a] text-slate-100 font-sans overflow-hidden selection:bg-blue-500/30">
      {/* Sidebar */}
      <aside className="w-80 bg-[#1e293b]/50 backdrop-blur-xl border-r border-slate-800 flex flex-col shadow-2xl overflow-y-auto custom-scrollbar">
        <div className="p-8 pb-4">
          <div className="text-2xl font-black tracking-tighter bg-gradient-to-br from-blue-400 via-indigo-400 to-purple-500 bg-clip-text text-transparent drop-shadow-sm">
            TARGARYEN
          </div>
          <div className="text-[10px] font-semibold text-slate-500 uppercase tracking-widest mt-1">Dynamic Intelligence Engine</div>
        </div>
        
        {/* Add Competitor Form */}
        <div className="px-6 py-4 border-b border-slate-800/50 mb-4">
           <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4 flex items-center"><span className="mr-2">🎯</span> Track Competitor</h3>
           <form onSubmit={addCompetitor} className="space-y-3">
              <input 
                 type="text" 
                 required
                 value={compName}
                 onChange={e => setCompName(e.target.value)}
                 className="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
                 placeholder="Entity Name (e.g. Stripe)"
              />
              <input 
                 type="url" 
                 required
                 value={compUrl}
                 onChange={e => setCompUrl(e.target.value)}
                 className="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
                 placeholder="URL (https://...)"
              />
              <button 
                 type="submit" 
                 disabled={adding}
                 className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white rounded-lg px-4 py-2 text-sm font-bold shadow-lg shadow-blue-500/20 transition-all border border-blue-500"
              >
                 {adding ? "Deploying Scraper..." : "Add Target URL"}
              </button>
           </form>
        </div>

        {/* Competitor List */}
        <div className="px-6 pb-6 flex-1">
           <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4 flex items-center"><span className="mr-2">📡</span> Active Targets</h3>
           {competitors.length === 0 ? (
               <div className="text-center p-4 border border-slate-700/50 rounded-xl bg-slate-900/30">
                  <p className="text-xs text-slate-500">No active targets locked.</p>
               </div>
           ) : (
             <div className="space-y-3">
               {competitors.map((c, i) => (
                 <div key={i} className="group relative bg-[#0f172a]/40 p-4 rounded-xl border border-slate-700/50 hover:border-blue-500/50 transition-colors">
                    <div className="absolute top-1/2 -left-[1px] -translate-y-1/2 w-1 h-0 bg-blue-500 group-hover:h-8 transition-all rounded-r-lg"></div>
                    <p className="text-sm font-bold text-slate-200 truncate">{c.name}</p>
                    <a href={c.url} target="_blank" rel="noreferrer" className="text-[10px] text-blue-400/80 hover:text-blue-300 truncate block mt-1">{c.url}</a>
                 </div>
               ))}
             </div>
           )}
        </div>
      </aside>

      {/* Main Panel */}
      <main className="flex-1 flex flex-col overflow-y-auto overflow-x-hidden relative">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-blue-900/10 via-[#0f172a]/0 to-transparent pointer-events-none"></div>
        
        <header className="sticky top-0 z-50 h-20 bg-[#0f172a]/80 backdrop-blur-md border-b border-slate-800/50 flex items-center justify-between px-10">
          <h2 className="text-2xl font-semibold text-slate-100 drop-shadow-sm">Live Signals</h2>
          <div className="flex items-center space-x-8">
             <button 
                onClick={triggerAnalysis} 
                disabled={loading || competitors.length === 0} 
                className="px-6 py-2.5 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-400 hover:to-purple-500 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg font-bold text-sm tracking-wide shadow-lg shadow-purple-500/25 transition-all outline-none ring-2 ring-purple-500/30 focus:ring-purple-400"
             >
               {loading ? "Crunching Diffs..." : "Evaluate Market Strategy"}
             </button>
          </div>
        </header>

        <div className="p-10 z-10 space-y-8 flex-1 flex flex-col">
           {/* Primary Empty State Validation UX */}
           {competitors.length === 0 ? (
              <div className="flex-1 flex flex-col items-center justify-center p-12 text-center max-w-lg mx-auto bg-slate-800/20 border border-slate-700/50 rounded-2xl shadow-xl backdrop-blur-sm">
                 <div className="w-20 h-20 bg-blue-500/10 rounded-full flex items-center justify-center mb-6 border border-blue-500/20 shadow-[0_0_30px_rgba(59,130,246,0.15)]">
                    <span className="text-4xl">📡</span>
                 </div>
                 <h2 className="text-2xl font-bold text-slate-200 mb-3">System Waiting</h2>
                 <p className="text-slate-400 text-sm leading-relaxed mb-6">Add your first competitor URL in the sidebar to begin tracking physical DOM changes.</p>
                 <div className="inline-flex px-4 py-2 bg-slate-900 text-slate-300 text-xs uppercase tracking-widest font-bold border border-slate-700 rounded-lg shadow-inner">
                    Awaiting Target Inputs
                 </div>
              </div>
           ) : (
             <>
               {/* Gemini Intelligence Panel */}
               {analysis && (
                  <div className="bg-gradient-to-br from-slate-800 to-[#1e293b] rounded-2xl p-8 border border-slate-700 shadow-2xl relative overflow-hidden">
                     <div className="absolute top-0 right-0 p-4 opacity-5 pointer-events-none">
                        <svg className="w-48 h-48" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/></svg>
                     </div>
                     
                     <div className="flex items-center gap-3 mb-8">
                        <div className="p-2 bg-indigo-500/20 rounded-lg border border-indigo-500/30">
                           <span className="text-2xl">🧠</span>
                        </div>
                        <h3 className="text-2xl font-bold bg-gradient-to-r from-indigo-300 to-purple-300 bg-clip-text text-transparent">Gemini Core Analysis</h3>
                     </div>
                     
                     {/* Added Trends Metric */}
                     <div className="mb-8 p-5 bg-indigo-500/5 border border-indigo-500/20 rounded-xl relative overflow-hidden group">
                        <div className="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-indigo-400 to-purple-500"></div>
                        <h4 className="text-xs uppercase tracking-widest text-indigo-400 font-bold mb-3">Macro Trends Detected</h4>
                        <p className="text-slate-200/90 leading-relaxed font-medium text-sm">{analysis.trends}</p>
                     </div>

                     <div className="grid grid-cols-1 md:grid-cols-2 gap-8 relative z-10">
                        <div className="space-y-2">
                           <h4 className="text-xs uppercase tracking-widest text-indigo-400 font-bold mb-3">Market Whitespace</h4>
                           <p className="text-slate-300 leading-relaxed font-light text-sm bg-slate-900/50 p-5 rounded-xl border border-slate-700/50">{analysis.whitespace}</p>
                        </div>
                        <div className="space-y-2">
                           <h4 className="text-xs uppercase tracking-widest text-emerald-400 font-bold mb-3">Recommended Strategy</h4>
                           <p className="text-slate-300 leading-relaxed font-light text-sm bg-slate-900/50 p-5 rounded-xl border border-slate-700/50">{analysis.strategy}</p>
                        </div>
                     </div>
                     
                     <div className="mt-8 bg-slate-900/80 p-6 rounded-xl border border-rose-500/20 shadow-[inset_0_2px_20px_rgba(0,0,0,0.4)]">
                        <h4 className="text-xs uppercase tracking-widest text-rose-400 font-bold mb-4 flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-rose-500 animate-pulse"></div> Aggressive Pricing Action</h4>
                        <p className="text-rose-100/90 leading-relaxed font-medium text-lg">{analysis.pricing_action}</p>
                     </div>

                     {analysis.evidence && (
                         <div className="mt-4 p-5 bg-amber-500/10 rounded-xl border border-amber-500/20 border-dashed">
                            <h4 className="text-[10px] uppercase tracking-widest text-amber-400 font-bold mb-2 flex items-center gap-2">🔍 AI Traceability / Source Evidence</h4>
                            <p className="text-xs text-amber-200/80 font-mono italic leading-relaxed">"{analysis.evidence}"</p>
                            <p className="text-[9px] text-amber-500/60 font-semibold mt-3 text-right uppercase tracking-widest">Source: Dynamic Snapshot Array</p>
                         </div>
                     )}
                  </div>
               )}

               {/* Metrics Grid */}
               <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  {/* Notifications List */}
                  <div className="bg-slate-800/80 backdrop-blur-md rounded-2xl p-8 border border-slate-700/50 shadow-xl">
                     <h3 className="text-lg font-semibold mb-6 flex items-center justify-between">
                        Priority Alerts
                        <span className="text-xs font-bold text-slate-400 uppercase tracking-widest bg-slate-900 px-3 py-1 rounded-full">Automated</span>
                     </h3>
                     <div className="space-y-4 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
                        {notifications.map((n: any, i: number) => (
                           <div key={i} className={`p-5 rounded-xl border ${n.score > 7 ? 'bg-rose-500/10 border-rose-500/30' : 'bg-[#0f172a]/50 border-slate-700'} relative overflow-hidden group hover:-translate-y-1 transition-transform`}>
                              <div className={`absolute left-0 top-0 bottom-0 w-1 ${n.score > 7 ? 'bg-rose-500 shadow-[0_0_10px_rgba(244,63,94,0.6)]' : 'bg-blue-500'}`}></div>
                              <div className="flex justify-between items-start mb-2">
                                 <div className={`text-[10px] font-black uppercase tracking-widest px-2 py-0.5 rounded ${n.score > 7 ? 'bg-rose-500/20 text-rose-400' : 'bg-blue-500/20 text-blue-400'}`}>
                                    Impact: {n.impactLevel} | Score {n.score}
                                 </div>
                                 <span className="text-xs text-slate-500 font-medium">{new Date(n.timestamp).toLocaleTimeString()}</span>
                              </div>
                              <p className="text-sm font-medium text-slate-200 leading-snug break-words">{n.message}</p>
                           </div>
                        ))}
                        {notifications.length === 0 && <p className="text-slate-500 text-sm text-center py-8">No alerts stored in database.</p>}
                     </div>
                  </div>

                  {/* Snapshot Timeline */}
                  <div className="bg-slate-800/80 backdrop-blur-md rounded-2xl p-8 border border-slate-700/50 shadow-xl">
                     <h3 className="text-lg font-semibold mb-6 flex items-center justify-between">
                        Pulse Trace Timeline
                        <span className="text-xs font-bold text-slate-400 uppercase tracking-widest bg-slate-900 px-3 py-1 rounded-full">Scraper Engine</span>
                     </h3>
                     <div className="relative border-l-2 border-slate-700 ml-3 space-y-8 max-h-[400px] overflow-y-auto pr-4 custom-scrollbar">
                        {snapshots.map((s: any, i: number) => {
                           const cName = competitors.find(c => c.id === s.competitorId)?.name || "Unknown";
                           return (
                             <div key={i} className="pl-6 relative">
                                <div className="absolute w-4 h-4 rounded-full bg-emerald-500 border-4 border-slate-800 -left-[9px] top-1 shadow-[0_0_10px_rgba(16,185,129,0.5)]"></div>
                                <div className="text-xs text-slate-400 font-bold mb-1 tracking-wider">{new Date(s.timestamp).toLocaleString()}</div>
                                <div className="p-4 bg-slate-900/50 rounded-xl border border-slate-700/50">
                                   <p className="text-xs font-bold text-white mb-2">{cName}</p>
                                   <p className="text-xs text-slate-300 font-mono break-all text-ellipsis overflow-hidden whitespace-nowrap">Hash: {s.contentHash}</p>
                                </div>
                             </div>
                           );
                        })}
                        {snapshots.length === 0 && <p className="text-slate-500 text-sm text-center py-8 ml-[-12px]">Awaiting system initial pull.</p>}
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
