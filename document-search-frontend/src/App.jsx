import React, { useState } from 'react';
import IngestionPanel from './components/IngestionPanel';
import SearchBar from './components/SearchBar';
import ResultList from './components/ResultList';
import './App.css';

const App = () => {
    const [results, setResults] = useState([]);
    const [loading, setLoading] = useState(false);

    return (
        <div className="app-container">
            <header className="app-header">
                <h1 className="app-main-title">Hệ Thống Tìm Kiếm Tài Liệu Phân Tán</h1>
                <p className="app-sub-title">
                  Phát triển trên nền tảng Spring Boot, ReactJS và Elasticsearch Cụm đa Node
                </p>
            </header>

            <main className="app-main-content">
                <IngestionPanel />
                <div className="app-search-section">
                    <h3 className="app-section-title">Tìm kiếm tài liệu toàn văn</h3>
                    <SearchBar setResults={setResults} setLoading={setLoading} />
                    <ResultList results={results} loading={loading} />
                </div>
            </main>
        </div>
    );
};

export default App;