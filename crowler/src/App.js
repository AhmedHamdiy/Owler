import React, { Component } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import './Styles/App.css';
import SearchPage from './Pages/MainPage.js';
import ResultsPage from './Pages/ResultsPage.js';
import Owl from './Styles/Owl.png'
import SearchBar from "./Components/SearchBar";



const Crowler = () => {
  return (
    <div>
    <div>
    <div className="main-container">
            <img src={Owl} alt="Owl"/>
              
        <div>
            <input required type="search" placeholder="Type queries, then hoot!!" className="query-text-box" 
            />
        <input type="submit" value="Hoot" className="hoot-button" />
        </div>
        </div> 
    {/* <BrowserRouter>
  <Routes>
    <Route exact path="/" element={<MainPage />} />
     <Route  exact path={`/:query`} element={<ResultsPage />}/>
    </Routes>
    </BrowserRouter> */}
    </div>
    </div>
  );
};

export default Crowler;