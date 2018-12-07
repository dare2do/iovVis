/*
 * Copyright © 2016-2018 The BeiDouApp Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import uiRouter from 'angular-ui-router';
import beidouappGrid from '../components/grid.directive';
import beidouappApiUser from '../api/user.service';
import beidouappApiAsset from '../api/asset.service';
import beidouappApiCustomer from '../api/customer.service';

import AssetRoutes from './asset.routes';
import {AssetController, AssetCardController} from './asset.controller';
import AssignAssetToCustomerController from './assign-to-customer.controller';
import AddAssetsToCustomerController from './add-assets-to-customer.controller';
import AssetDirective from './asset.directive';

export default angular.module('beidouapp.asset', [
    uiRouter,
    beidouappGrid,
    beidouappApiUser,
    beidouappApiAsset,
    beidouappApiCustomer
])
    .config(AssetRoutes)
    .controller('AssetController', AssetController)
    .controller('AssetCardController', AssetCardController)
    .controller('AssignAssetToCustomerController', AssignAssetToCustomerController)
    .controller('AddAssetsToCustomerController', AddAssetsToCustomerController)
    .directive('tbAsset', AssetDirective)
    .name;
