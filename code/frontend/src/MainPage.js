import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import './App.css';
import axios from 'axios';
import Owl from './Owl.png'
import SearchBar from "./SearchBar";



const MainPage =() => {
    const [query,setQuery]=useState('');
    const navigate=useNavigate();

    return (
        <div className="main-container">
            <img src={Owl} alt="Owl"/>
          {/*
              <SearchBar searchedQuery=''/>
            */
          } 
        </div>
    );

}

export default MainPage;