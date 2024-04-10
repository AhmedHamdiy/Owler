import React, { Component } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import logo from './logo.svg';
import './App.css';
import MainPage from './MainPage';
import ResultsPage from './ResultsPage';



const Crowler = () => {
  return (
    
    <BrowserRouter>
  <Routes>
    <Route path="/" element={<MainPage />} />
     <Route exact path={`/:query`} element={<ResultsPage />}/>
    </Routes>
    </BrowserRouter>
  );
};

export default Crowler;
