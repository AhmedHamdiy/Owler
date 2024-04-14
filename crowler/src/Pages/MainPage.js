import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import  '../Styles/App.css';
import Owl from '../Styles/Owl.png'
import SearchBar from "../Components/SearchBar";



const SearchPage =() => {
    const [query,setQuery]=useState('');
    const navigate=useNavigate();

    return (
         <div className="main-container">
            <img src={Owl} alt="Owl"/>
              {/* <SearchBar searchedQuery=''/>  */}
        </div> 
    );

}

export default SearchPage;